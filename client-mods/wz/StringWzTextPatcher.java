import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Applies the Comfy Coin text change to this project's small custom Data.wz override.
 *
 * This is intentionally a narrowly-scoped writer: it validates the known pristine
 * input and changes one encrypted ASCII property while keeping internal string references
 * and enclosing property block sizes valid after the two-byte size reduction.
 */
public final class StringWzTextPatcher {
    private static final String BASE_SHA256 = "8BFDC1EE0C32846D9210F25A25A7E7604325C9A0D505DC542AC7C5C5BCB00DD2";
    private static final String OLD_TEXT = "Swordie Coin";
    private static final String NEW_TEXT = "Comfy Coin";

    private StringWzTextPatcher() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: StringWzTextPatcher <source Data.wz> <destination Data.wz>");
        }

        Path source = Path.of(args[0]).toAbsolutePath().normalize();
        Path destination = Path.of(args[1]).toAbsolutePath().normalize();
        byte[] sourceBytes = Files.readAllBytes(source);
        String sourceHash = sha256(sourceBytes);
        byte[] patchedBytes;
        if (BASE_SHA256.equals(sourceHash)) {
            patchedBytes = patch(sourceBytes);
        } else {
            require(containsExpectedText(sourceBytes, NEW_TEXT), "Unexpected source Data.wz hash: " + sourceHash);
            patchedBytes = sourceBytes;
        }
        String patchedHash = sha256(patchedBytes);

        if (Files.exists(destination)) {
            String destinationHash = sha256(Files.readAllBytes(destination));
            if (patchedHash.equals(destinationHash)) {
                System.out.println("Data.wz already contains the Comfy Coin change.");
                System.out.println("SHA-256: " + patchedHash);
                return;
            }
            require(BASE_SHA256.equals(destinationHash), "Refusing to overwrite an unexpected destination Data.wz hash: " + destinationHash);
        }

        Files.createDirectories(destination.getParent());
        Path temporary = destination.resolveSibling(destination.getFileName() + ".comfystory.tmp");
        Files.write(temporary, patchedBytes);
        Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        System.out.println("Updated " + destination);
        System.out.println("SHA-256: " + patchedHash);
    }

    private static byte[] patch(byte[] source) {
        ImageScan imageScan = scanImage(source, 0, source.length);
        List<StringLocation> oldTextLocations = imageScan.strings.stream()
                .filter(location -> OLD_TEXT.equals(location.value)).toList();
        require(oldTextLocations.size() == 1, "Expected exactly one Swordie Coin string property, found " + oldTextLocations.size()
                + " among " + imageScan.strings.size() + " string properties (first: "
                + imageScan.strings.stream().limit(5).map(location -> location.value).toList() + ").");
        StringLocation oldTextLocation = oldTextLocations.getFirst();

        byte[] oldEncodedText = Arrays.copyOfRange(source, oldTextLocation.position, oldTextLocation.position + oldTextLocation.length);
        byte[] newEncodedText = encodeAsciiWzString(NEW_TEXT);
        int textOffset = oldTextLocation.position;

        byte[] patched = new byte[source.length - (oldEncodedText.length - newEncodedText.length)];
        System.arraycopy(source, 0, patched, 0, textOffset);
        System.arraycopy(newEncodedText, 0, patched, textOffset, newEncodedText.length);
        System.arraycopy(source, textOffset + oldEncodedText.length, patched, textOffset + newEncodedText.length,
                source.length - textOffset - oldEncodedText.length);

        int sizeDelta = newEncodedText.length - oldEncodedText.length;
        require(sizeDelta == -2, "Unexpected text size delta.");
        for (ExtendedBlock block : imageScan.extendedBlocks) {
            if (block.contentStart <= textOffset && textOffset < block.contentEnd) {
                int newSizePosition = block.sizePosition > textOffset ? block.sizePosition + sizeDelta : block.sizePosition;
                writeInt(patched, newSizePosition, block.size + sizeDelta);
            }
        }
        for (StringReference reference : imageScan.references) {
            if (reference.target > textOffset) {
                int newPointerPosition = reference.pointerPosition > textOffset ? reference.pointerPosition + sizeDelta : reference.pointerPosition;
                writeInt(patched, newPointerPosition, reference.target + sizeDelta);
            }
        }

        return patched;
    }

    private static boolean containsExpectedText(byte[] data, String expectedText) {
        try {
            return scanImage(data, 0, data.length).strings.stream().anyMatch(location -> expectedText.equals(location.value));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static List<ImageEntry> readRootEntries(byte[] data, int fileStart, int hash) {
        Cursor cursor = new Cursor(fileStart);
        int count = readCompressedInt(data, cursor);
        require(count > 0 && count < 256, "Unexpected root image count: " + count);

        List<ImageEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int type = readUnsignedByte(data, cursor);
            require(type == 4, "Unexpected root entry type: " + type);
            String name = readWzString(data, cursor);
            int sizePosition = cursor.position;
            int size = readCompressedInt(data, cursor);
            int sizeLength = cursor.position - sizePosition;
            int checksumPosition = cursor.position;
            int checksum = readCompressedInt(data, cursor);
            int checksumLength = cursor.position - checksumPosition;
            int offsetPosition = cursor.position;
            int offset = decodeOffset(offsetPosition, fileStart, hash, readInt(data, cursor.position));
            cursor.position += Integer.BYTES;
            entries.add(new ImageEntry(name, size, checksum, offset, sizePosition, sizeLength, checksumPosition, checksumLength, offsetPosition));
        }
        return entries;
    }

    private static int decodeOffset(int position, int fileStart, int hash, int encodedOffset) {
        int offset = position - fileStart;
        offset = ~offset;
        offset *= hash;
        offset -= 0x581C3F6D;
        offset = Integer.rotateLeft(offset, offset & 0x1F);
        offset ^= encodedOffset;
        return offset + fileStart * 2;
    }

    private static int encodeOffset(int position, int fileStart, int hash, int targetOffset) {
        int offset = position - fileStart;
        offset = ~offset;
        offset *= hash;
        offset -= 0x581C3F6D;
        offset = Integer.rotateLeft(offset, offset & 0x1F);
        return offset ^ (targetOffset - fileStart * 2);
    }

    private static int versionHash(int version) {
        int hash = 0;
        for (char character : Integer.toString(version).toCharArray()) {
            hash = (hash << 5) + character + 1;
        }
        return hash;
    }

    private static byte[] encodeAsciiWzString(String value) {
        byte[] plain = value.getBytes(StandardCharsets.US_ASCII);
        require(plain.length <= 127, "Only short ASCII WZ strings are supported.");
        byte[] encoded = new byte[plain.length + 1];
        encoded[0] = (byte) -plain.length;
        for (int i = 0; i < plain.length; i++) {
            encoded[i + 1] = (byte) (plain[i] ^ (0xAA + i));
        }
        return encoded;
    }

    private static ImageScan scanImage(byte[] data, int imageStart, int imageEnd) {
        Cursor cursor = new Cursor(imageStart);
        StringLocation imageType = readStringBlock(data, cursor, imageStart, new ArrayList<>());
        require("Property".equals(imageType.value), "Etc.img is not a Property image.");
        cursor.position += Short.BYTES;

        ImageScan scan = new ImageScan();
        scanProperties(data, cursor, imageStart, imageEnd, scan);
        return scan;
    }

    private static void scanProperties(byte[] data, Cursor cursor, int imageStart, int end, ImageScan scan) {
        int count = readCompressedInt(data, cursor);
        require(count >= 0 && count < 1_000_000, "Unexpected property count: " + count);
        for (int i = 0; i < count; i++) {
            readStringBlock(data, cursor, imageStart, scan.references);
            int type = readUnsignedByte(data, cursor);
            switch (type) {
                case 0x00 -> { }
                case 0x02, 0x0B -> cursor.position += Short.BYTES;
                case 0x03 -> readCompressedInt(data, cursor);
                case 0x04 -> {
                    int marker = readUnsignedByte(data, cursor);
                    if (marker == 0x80) cursor.position += Float.BYTES;
                }
                case 0x05 -> cursor.position += Double.BYTES;
                case 0x08 -> {
                    StringLocation value = readStringBlock(data, cursor, imageStart, scan.references);
                    scan.strings.add(value);
                }
                case 0x09 -> {
                    int sizePosition = cursor.position;
                    int size = readInt(data, cursor.position);
                    cursor.position += Integer.BYTES;
                    int contentStart = cursor.position;
                    int contentEnd = Math.addExact(contentStart, size);
                    require(contentEnd <= end, "Extended property exceeds its parent block.");
                    scan.extendedBlocks.add(new ExtendedBlock(sizePosition, size, contentStart, contentEnd));
                    StringLocation extendedType = readStringBlock(data, cursor, imageStart, scan.references);
                    if ("Property".equals(extendedType.value)) {
                        cursor.position += Short.BYTES;
                        scanProperties(data, cursor, imageStart, contentEnd, scan);
                    }
                    cursor.position = contentEnd;
                }
                case 0x14 -> readCompressedLong(data, cursor);
                default -> throw new IllegalStateException("Unsupported Etc.img property type: " + type);
            }
            require(cursor.position <= end, "Property exceeds its enclosing block.");
        }
    }

    private static StringLocation readStringBlock(byte[] data, Cursor cursor, int imageStart, List<StringReference> references) {
        int blockType = readUnsignedByte(data, cursor);
        if (blockType == 0x00 || blockType == 0x73) {
            int position = cursor.position;
            StringLocation result = readWzString(data, position);
            cursor.position += result.length;
            return result;
        }
        if (blockType == 0x01 || blockType == 0x1B) {
            int pointerPosition = cursor.position;
            int target = imageStart + readInt(data, pointerPosition);
            cursor.position += Integer.BYTES;
            StringLocation result = readWzString(data, target);
            references.add(new StringReference(pointerPosition, target));
            return result;
        }
        throw new IllegalStateException("Unsupported WZ string block type: " + blockType);
    }

    private static StringLocation readWzString(byte[] data, int position) {
        int marker = (byte) data[position];
        if (marker == 0) return new StringLocation(position, 1, "");
        if (marker > 0) return new StringLocation(position, 1 + marker * 2, "<unicode>");
        int length = marker == Byte.MIN_VALUE ? readInt(data, position + 1) : -marker;
        int prefixLength = marker == Byte.MIN_VALUE ? 5 : 1;
        byte[] plain = new byte[length];
        for (int i = 0; i < length; i++) {
            plain[i] = (byte) (Byte.toUnsignedInt(data[position + prefixLength + i]) ^ (0xAA + i));
        }
        return new StringLocation(position, prefixLength + length, new String(plain, StandardCharsets.US_ASCII));
    }

    private static int checksum(byte[] data, int from, int to) {
        int checksum = 0;
        for (int i = from; i < to; i++) {
            checksum += Byte.toUnsignedInt(data[i]);
        }
        return checksum;
    }

    private static String readWzString(byte[] data, Cursor cursor) {
        int marker = (byte) readUnsignedByte(data, cursor);
        if (marker == 0) {
            return "";
        }
        if (marker > 0) {
            cursor.position += marker * 2;
            return "<unicode>";
        }
        int length = -marker;
        byte[] plain = new byte[length];
        for (int i = 0; i < length; i++) {
            plain[i] = (byte) (readUnsignedByte(data, cursor) ^ (0xAA + i));
        }
        return new String(plain, StandardCharsets.US_ASCII);
    }

    private static int readCompressedInt(byte[] data, Cursor cursor) {
        int value = (byte) readUnsignedByte(data, cursor);
        return value == Byte.MIN_VALUE ? readInt(data, advance(cursor, Integer.BYTES)) : value;
    }

    private static long readCompressedLong(byte[] data, Cursor cursor) {
        int value = (byte) readUnsignedByte(data, cursor);
        if (value != Byte.MIN_VALUE) return value;
        int position = advance(cursor, Long.BYTES);
        return readLong(data, position);
    }

    private static int readUnsignedByte(byte[] data, Cursor cursor) {
        require(cursor.position < data.length, "Unexpected end of String.wz.");
        return Byte.toUnsignedInt(data[cursor.position++]);
    }

    private static int advance(Cursor cursor, int amount) {
        int position = cursor.position;
        cursor.position += amount;
        return position;
    }

    private static void writeCompressedInt(byte[] data, int position, int value, int expectedLength) {
        int length = value > -128 && value < 128 ? 1 : 5;
        require(length == expectedLength, "WZ directory integer length changed unexpectedly.");
        if (length == 1) {
            data[position] = (byte) value;
        } else {
            data[position] = Byte.MIN_VALUE;
            writeInt(data, position + 1, value);
        }
    }

    private static int readInt(byte[] data, int position) {
        return Byte.toUnsignedInt(data[position])
                | (Byte.toUnsignedInt(data[position + 1]) << 8)
                | (Byte.toUnsignedInt(data[position + 2]) << 16)
                | (Byte.toUnsignedInt(data[position + 3]) << 24);
    }

    private static long readLong(byte[] data, int position) {
        long value = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            value |= (long) Byte.toUnsignedInt(data[position + i]) << (i * 8);
        }
        return value;
    }

    private static void writeInt(byte[] data, int position, int value) {
        for (int i = 0; i < Integer.BYTES; i++) {
            data[position + i] = (byte) (value >>> (i * 8));
        }
    }

    private static void writeLong(byte[] data, int position, long value) {
        for (int i = 0; i < Long.BYTES; i++) {
            data[position + i] = (byte) (value >>> (i * 8));
        }
    }

    private static String sha256(byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) {
                result.append(String.format("%02X", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static final class Cursor {
        private int position;

        private Cursor(int position) {
            this.position = position;
        }
    }

    private static final class ImageScan {
        private final List<StringLocation> strings = new ArrayList<>();
        private final List<StringReference> references = new ArrayList<>();
        private final List<ExtendedBlock> extendedBlocks = new ArrayList<>();
    }

    private record StringLocation(int position, int length, String value) {
    }

    private record StringReference(int pointerPosition, int target) {
    }

    private record ExtendedBlock(int sizePosition, int size, int contentStart, int contentEnd) {
    }

    private record ImageEntry(String name, int size, int checksum, int offset, int sizePosition, int sizeLength,
                              int checksumPosition, int checksumLength, int offsetPosition) {
    }
}
