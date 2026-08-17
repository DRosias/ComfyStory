package net.swordie.tools;

import net.swordie.ms.ServerConstants;
import net.swordie.ms.enums.AccountType;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Console;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * One-time, local-only cleanup for the four legacy Swordie seed admin users.
 *
 * <p>This utility deliberately has no web/API entry point. It preserves user ID 1,
 * changes that user's login credentials, and deletes IDs 2-4 only when they still
 * match the known empty seed accounts.</p>
 */
public final class LegacyAdminCleanupTool {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3307/comfystory232";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD_ENV = "COMFYSTORY_DB_PASSWORD";
    private static final int MIN_USERNAME_LENGTH = 4;
    private static final int MAX_USERNAME_LENGTH = 255;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 255;
    private static final int ADMIN_ACCOUNT_TYPE = AccountType.Admin.ordinal();
    private static final Map<Integer, String> UNUSED_SEED_USERS = Map.of(
            2, "admin1",
            3, "asura",
            4, "maigal"
    );
    private static final DateTimeFormatter BACKUP_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private LegacyAdminCleanupTool() {
    }

    public static void main(String[] args) {
        boolean dryRun = false;
        for (String arg : args) {
            if ("--dry-run".equals(arg)) {
                dryRun = true;
            } else {
                System.err.println("Unknown argument: " + arg);
                System.err.println("Usage: LegacyAdminCleanupTool [--dry-run]");
                System.exit(2);
            }
        }

        try {
            run(dryRun);
        } catch (CleanupException e) {
            System.err.println("Cleanup stopped: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Cleanup failed safely: " + safeMessage(e));
            System.exit(1);
        }
    }

    private static void run(boolean dryRun) throws Exception {
        SeedState initialState;
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            initialState = inspectAndValidate(connection, dryRun);
            connection.rollback();
        }

        printState(initialState);
        if (dryRun) {
            System.out.println("Dry run complete. No database changes or backup were made.");
            return;
        }

        ensureServerIsStopped();
        Console console = System.console();
        if (console == null) {
            throw new CleanupException("An interactive console is required so the password is not echoed. Run the provided PowerShell or CMD launcher in a terminal.");
        }

        String username = validateUsername(console.readLine("New username for account ID 1: "));
        char[] firstPassword = console.readPassword("New password (8-255 characters): ");
        char[] secondPassword = console.readPassword("Verify new password: ");
        try {
            validatePassword(firstPassword);
            if (!Arrays.equals(firstPassword, secondPassword)) {
                throw new CleanupException("The passwords did not match.");
            }

            String confirmation = console.readLine(
                    "Type APPLY to rename ID 1 to '%s' and delete empty IDs 2-4: ", username);
            if (!"APPLY".equals(confirmation)) {
                throw new CleanupException("Confirmation was not entered; nothing was changed.");
            }

            Path backup = createBackup();
            String passwordHash = BCrypt.hashpw(
                    new String(firstPassword), BCrypt.gensalt(ServerConstants.BCRYPT_ITERATIONS));
            applyCleanup(username, passwordHash);

            System.out.println("Legacy Admin cleanup completed successfully.");
            System.out.println("Account ID 1 is now named: " + username);
            System.out.println("Backup: " + backup.toAbsolutePath());
        } finally {
            if (firstPassword != null) {
                Arrays.fill(firstPassword, '\0');
            }
            if (secondPassword != null) {
                Arrays.fill(secondPassword, '\0');
            }
        }
    }

    private static Connection openConnection() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", DB_USER);
        String password = System.getenv(DB_PASSWORD_ENV);
        if (password != null) {
            properties.setProperty("password", password);
        }
        return DriverManager.getConnection(DB_URL, properties);
    }

    private static SeedState inspectAndValidate(Connection connection, boolean lockRows)
            throws SQLException, CleanupException {
        int totalUsers;
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            totalUsers = resultSet.getInt(1);
        }
        if (totalUsers != 4) {
            throw new CleanupException("Expected exactly four legacy users, but found " + totalUsers + ".");
        }

        String userQuery = "SELECT id, name, accounttype FROM users WHERE id IN (1, 2, 3, 4) ORDER BY id"
                + (lockRows ? " FOR UPDATE" : "");
        Map<Integer, SeedUser> users = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(userQuery);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                users.put(id, new SeedUser(
                        id,
                        resultSet.getString("name"),
                        resultSet.getInt("accounttype"),
                        countOwnedRows(connection, "accounts", id),
                        countOwnedRows(connection, "characters", id)
                ));
            }
        }

        if (users.size() != 4 || !users.keySet().containsAll(List.of(1, 2, 3, 4))) {
            throw new CleanupException("User IDs 1-4 do not exactly match the expected legacy seed set.");
        }

        SeedUser retained = users.get(1);
        if (retained.accountType() != ADMIN_ACCOUNT_TYPE) {
            throw new CleanupException("Account ID 1 is no longer an Admin account.");
        }
        if (retained.accountRows() < 1 || retained.characters() < 1) {
            throw new CleanupException("Account ID 1 no longer owns the expected account and character data.");
        }

        for (Map.Entry<Integer, String> expected : UNUSED_SEED_USERS.entrySet()) {
            SeedUser user = users.get(expected.getKey());
            if (!expected.getValue().equals(user.name())) {
                throw new CleanupException("User ID " + user.id() + " is named '" + user.name()
                        + "', not the expected empty seed user '" + expected.getValue() + "'.");
            }
            if (user.accountType() != ADMIN_ACCOUNT_TYPE) {
                throw new CleanupException("User ID " + user.id() + " is no longer an Admin account.");
            }
            if (user.accountRows() != 0 || user.characters() != 0) {
                throw new CleanupException("User ID " + user.id()
                        + " now owns account or character data and will not be deleted.");
            }
        }

        return new SeedState(retained, List.of(users.get(2), users.get(3), users.get(4)));
    }

    private static int countOwnedRows(Connection connection, String table, int userId) throws SQLException {
        String sql;
        if ("accounts".equals(table)) {
            sql = "SELECT COUNT(*) FROM accounts WHERE userid = ?";
        } else if ("characters".equals(table)) {
            sql = "SELECT COUNT(*) FROM characters WHERE userid = ?";
        } else {
            throw new IllegalArgumentException("Unsupported ownership table: " + table);
        }
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private static void applyCleanup(String username, String passwordHash)
            throws SQLException, CleanupException {
        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                inspectAndValidate(connection, true);
                ensureUsernameAvailable(connection, username);

                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE users SET name = ?, password = ? WHERE id = 1")) {
                    statement.setString(1, username);
                    statement.setString(2, passwordHash);
                    if (statement.executeUpdate() != 1) {
                        throw new CleanupException("Account ID 1 was not updated exactly once.");
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM users WHERE id IN (2, 3, 4)")) {
                    if (statement.executeUpdate() != 3) {
                        throw new CleanupException("The three unused seed accounts were not deleted exactly once each.");
                    }
                }

                verifyFinalState(connection, username);
                connection.commit();
            } catch (SQLException | CleanupException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private static void ensureUsernameAvailable(Connection connection, String username)
            throws SQLException, CleanupException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM users WHERE name = ? AND id <> 1")) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    throw new CleanupException("The username '" + username + "' is already in use.");
                }
            }
        }
    }

    private static void verifyFinalState(Connection connection, String username)
            throws SQLException, CleanupException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name, accounttype FROM users" );
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()
                    || resultSet.getInt("id") != 1
                    || !username.equals(resultSet.getString("name"))
                    || resultSet.getInt("accounttype") != ADMIN_ACCOUNT_TYPE
                    || resultSet.next()) {
                throw new CleanupException("Final user-table verification failed.");
            }
        }
        if (countOwnedRows(connection, "accounts", 1) < 1
                || countOwnedRows(connection, "characters", 1) < 1) {
            throw new CleanupException("Account ID 1 lost ownership data; rolling back.");
        }
    }

    private static Path createBackup() throws IOException, InterruptedException, CleanupException {
        Path dumpExecutable = findDumpExecutable();
        Path backupDirectory = Path.of("backups", "account-security");
        Files.createDirectories(backupDirectory);
        Path backupFile = backupDirectory.resolve(
                "legacy-admin-users-" + BACKUP_TIMESTAMP.format(LocalDateTime.now()) + ".sql");

        List<String> command = new ArrayList<>();
        command.add(dumpExecutable.toString());
        command.add("--host=127.0.0.1");
        command.add("--port=3307");
        command.add("--user=" + DB_USER);
        command.add("--no-create-info");
        command.add("--complete-insert");
        command.add("--skip-extended-insert");
        command.add("--where=id IN (1,2,3,4)");
        command.add("comfystory232");
        command.add("users");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        String password = System.getenv(DB_PASSWORD_ENV);
        if (password != null) {
            processBuilder.environment().put("MYSQL_PWD", password);
        }
        processBuilder.redirectOutput(backupFile.toFile());
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        int exitCode = processBuilder.start().waitFor();
        if (exitCode != 0 || !Files.isRegularFile(backupFile) || Files.size(backupFile) == 0) {
            Files.deleteIfExists(backupFile);
            throw new CleanupException("The database backup failed; no account changes were attempted.");
        }
        return backupFile;
    }

    private static Path findDumpExecutable() throws CleanupException {
        String configured = System.getenv("COMFYSTORY_MYSQL_DUMP");
        List<Path> candidates = new ArrayList<>();
        if (configured != null && !configured.isBlank()) {
            candidates.add(Path.of(configured));
        }
        candidates.add(Path.of("C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe"));

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        throw new CleanupException("MySQL dump utility was not found. Set COMFYSTORY_MYSQL_DUMP to its full path.");
    }

    private static void ensureServerIsStopped() throws CleanupException {
        if (isPortOpen(3000) || isPortOpen(ServerConstants.LOGIN_PORT)) {
            throw new CleanupException("Stop the ComfyStory server first (ports 3000 or 8484 are still listening).");
        }
    }

    private static boolean isPortOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 250);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    static String validateUsername(String username) throws CleanupException {
        if (username == null) {
            throw new CleanupException("A username is required.");
        }
        String normalized = username.trim();
        if (normalized.length() < MIN_USERNAME_LENGTH || normalized.length() > MAX_USERNAME_LENGTH) {
            throw new CleanupException("Username must be between 4 and 255 characters.");
        }
        return normalized;
    }

    static void validatePassword(char[] password) throws CleanupException {
        if (password == null
                || password.length < MIN_PASSWORD_LENGTH
                || password.length > MAX_PASSWORD_LENGTH) {
            throw new CleanupException("Password must be between 8 and 255 characters.");
        }
    }

    private static void printState(SeedState state) {
        SeedUser retained = state.retained();
        System.out.printf(
                "Retain: ID %d, username '%s', %d account row(s), %d character(s), Admin%n",
                retained.id(), retained.name(), retained.accountRows(), retained.characters());
        for (SeedUser user : state.removed()) {
            System.out.printf(
                    "Remove: ID %d, username '%s', %d account row(s), %d character(s), Admin%n",
                    user.id(), user.name(), user.accountRows(), user.characters());
        }
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message;
    }

    record SeedUser(int id, String name, int accountType, int accountRows, int characters) {
    }

    record SeedState(SeedUser retained, List<SeedUser> removed) {
    }

    static final class CleanupException extends Exception {
        CleanupException(String message) {
            super(message);
        }
    }
}
