package net.swordie.ms.world.gach;

import net.swordie.ms.util.Randomizer;
import net.swordie.ms.world.gach.result.GachaponDlgType;

import java.util.*;

public class GachaponConstants {
    private static final Map<GachaponDlgType, GachaponInfo> gachaponInfo = new HashMap<>();
    private static List<GachaponInfo.GachItem> equipments;
    private static List<GachaponInfo.GachItem> nebulites;

    static {
        init();
    }

    public static Map<GachaponDlgType, GachaponInfo> getGachaponInfo() {
        return gachaponInfo;
    }

    public static void init() {
        initNebulite();
        initEquipment();
        List<GachaponInfo.GachItem> items;
        // Town Gachapon
        String[] messages = new String[]{"ComfyStory Town Gachapon", "Text 1", "Text 2"};
        items = new ArrayList<>(equipments);
        items.addAll(nebulites);
        gachaponInfo.put(GachaponDlgType.TOWN, new GachaponInfo(Arrays.asList(messages), items));

        // Nebulite Gachapon
        messages = new String[]{"ComfyStory Nebulite Gachapon", "Text 1", "Text 2"};
        items = new ArrayList<>(nebulites);
        gachaponInfo.put(GachaponDlgType.NEBULITE, new GachaponInfo(Arrays.asList(messages), items));

        // Chair Gachapon
        messages = new String[]{"ComfyStory Chair Gachapon", "Text 1", "Text 2"};
        items = new ArrayList<>();
        int[] chairRewards = new int[]{
                3018000, // Prism Instant Camera Chair
                3018013, // Winter Snow Globe Chair
                3018023, // Cocoa Teacup Chair
                3018042, // Plum Blossom Window Chair
                3018050, // Whale Cloud Chair
                3018053, // Mushroom Carousel Chair
                3018056, // Fluffy Corgi Cushion Chair
                3018064, // Cozy Egg Chair
                3018070, // Toasty Morning Chair
                3018074, // Golden Tree Chair
                3018076, // Spring Rain Chair
                3018083, // Sundae Universe Chair
                3018089, // Enchanting Aria Chair
                3018090, // Cake Castle Chair
                3018126, // Terrarium Chair
                3018137, // Chilled Watermelon Chair
                3018153, // Fresh Flower Swing Chair
                3018175, // Snoozing Kitty Chair
                3018183, // Cherry Blossom Chair
                3018195, // Celestial Light Chair
                3018212, // Starry Hill Chair
                3018224, // Window on the Farm Chair
                3018246, // Cozy Cat Cuddles Chair
                3018251, // Antique Birdcage Chair
                3018264, // Blanketed in Flowers Chair
                3018266, // Leaf Umbrella Chair
                3018267, // Intergalactic Planetary Cat
                3018270, // Chocolate Spa
                3018272, // Fluffy Snowy Teddy
                3018312, // Rainbow Xylophone Chair
                3018335, // Celestial Whale Chair
                3018338, // Moonlight Serenade Chair
        };
        for (int itemId : chairRewards) {
            items.add(new GachaponInfo.GachItem(itemId));
        }
        gachaponInfo.put(GachaponDlgType.CHAIR, new GachaponInfo(Arrays.asList(messages), items));

        // Mount Gachapon
        messages = new String[]{"ComfyStory Mount Gachapon", "Text 1", "Text 2"};
        items = new ArrayList<>();
        int[] mountRewards = new int[]{
                2430299, // Permanent Magic Broom Mount Coupon
                2430354, // Permanent Giant Rabbit Mount Coupon
                2431422, // Wyvern Grump Mount Permanent Coupon
                2431473, // Permanent Pink Bean Balloon Mount Coupon
                2431765, // Permanent Shaken Wooden Horse Mount Coupon
                2431797, // Permanent Pegasus Mount Coupon
                2431898, // Permanent Bouncing Car Mount Coupon
                2431915, // Permanent Pelican Mount Coupon
                2432030, // Gargoyle Mount Permanent Coupon
                2432078, // Permanent Hellhound Mount Coupon
                2432085, // Permanent Dolphin Mount Coupon
                2432151, // Permanent Flying Bed Mount Coupon
                2432218, // Nina's Pentacle Mount Permanent Coupon
                2432291, // Permanent Skateboard Mount Coupon
                2432293, // Permanent Pumpkin Carriage Mount Coupon
                2432359, // Permanent Archangel Michael Mount Coupon
                2432361, // Permanent Devil Lucifer Mount Coupon
                2432451, // Planet B612 Permanent Riding Mount Coupon
                2432498, // Permanent Blue Flame Nightmare Mount Coupon
                2432552, // Honey Butterfly Mount Permanent Coupon
                2432653, // Flower Petal Prop Mount Permanent Coupon
                2432733, // Permanent Eagle Mount Coupon
                2432735, // Permanent Panda Mount Coupon
                2432751, // Permanent Helicopter Mount Coupon
                2432806, // Permanent Your Highness Mount Coupon
                2432821, // Water Scooter Mount Permanent Coupon
                2433053, // Jet Boat Mount Permanent Coupon
                2433128, // Permanent Vampire Phantom's Bat Mount Coupon
                2433169, // Permanent Canoe Mount Coupon
                2433170, // Permanent Minicopter Mount Coupon
                2433198, // Permanent Feline Pirate Ship Mount Coupon
                2433293, // Permanent Bling-Bling Rudolph Sled Mount
                2433811, // Permanent Strawberry Cake Mount Coupon
                2433946, // Penguin Pack Mount Permanent Coupon
                2433948, // Precious Pirates Mount Permanent Coupon
                2434235, // Transformed Cloud Mount Permanent Coupon
                2434236, // I'm a Dolphin Mount Permanent Coupon
                2434275, // Permanent Surfboard Mount Coupon
                2434277, // Permanent Aurora Doe Mount Coupon
                2434377, // Permanent Stroller Mount Coupon
                2434379, // Cygnus Knights Palanquin Mount Permanent Coupon
                2434515, // Permanent Wriggling Mount Coupon
                2434517, // Permanent Midnight Train Mount Coupon
                2434603, // Permanent High Quality Single Passenger Classic Car Mount Coupon
                2434649, // Permanent Wind Breaker Mount Coupon
                2434735, // Pterosaur Mount (Permanent) Coupon
                2434737, // Permanent Origami Boat Mount Coupon
                2435089, // Steam Cylinder Wing Mount (Permanent) Coupon
                2435091, // Snow Blossom Pentacle Mount (Permanent) Coupon
                2435112, // Frog Wagon Mount (Permanent) Coupon
                2435113, // Ostrich Wagon Mount (Permanent) Coupon
                2435114, // Camel Wagon Mount (Permanent) Coupon
                2435203, // Permanent Superhero Mount Coupon
                2435205, // Orange Snail Mount (Permanent) Coupon
                2435375, // Happy Car Mount Permanent Coupon
                2435553, // Permanent Monster Hot Air Balloon Mount Coupon
                2435947, // Permanent Friendly Ghost Mount Coupon
                2436315, // Permanent Mad Mimet Mount Coupon
        };
        for (int itemId : mountRewards) {
            items.add(new GachaponInfo.GachItem(itemId));
        }
        gachaponInfo.put(GachaponDlgType.MOUNT, new GachaponInfo(Arrays.asList(messages), items));

        // Special Gachapon
        messages = new String[]{"ComfyStory Special Gachapon", "Text 1", "Text 2"};
        items = new ArrayList<>();
        int[] specialRewards = new int[]{
                2631034, // Sealing Wax Damage Skin
                2631037, // Modern Art Damage Skin
                2631149, // Sparkles Damage Skin
                2631484, // Error Message Damage Skin
                2631485, // Glitch Damage Skin
                2631492, // Stained Glass Damage Skin
                2631627, // Extravagameza Damage Skin
                2631749, // Reverse Damage Skin
                2631884, // Color Light Damage Skin
                2631892, // Crystal Gold Damage Skin (Unit)
                2631932, // Pastel Animal Damage Skin
                2631994, // Cactus Damage Skin
                2632123, // Awake Damage Skin (Unit)
                2632187, // Autumn Leaves Damage Skin
                2632209, // Cappuccino Damage Skin
                2632348, // Cotton Candy Damage Skin (Unit)
                2632374, // Snowstorm Damage Skin
                2632429, // Autumn Sunset Damage Skin
                2632736, // Candle Damage Skin
                2632745, // Nostalgia Damage Skin
                2632815, // Aurora Damage Skin (Unit)
                2632964, // Flower Garden Damage Skin
                2632967, // Honey Bee Damage Skin
                2633047, // Resplendent Damage Skin (Unit)
                2633052, // Arabian Nights Damage Skin
                2633055, // Lotus Flower Damage Skin
                2633073, // Scorching Heat Damage Skin (Unit)
                2633218, // Pink Bean Crayon Damage Skin
                2633220, // Yeti Crayon Damage Skin
                2633277, // Wings Damage Skin
                2633305, // Blooming Forest Damage Skin (Unit)
                2633312, // Rock Spirit Damage Skin
                2633475, // Desert Sunset Damage Skin
                2633478, // Desert Oasis Damage Skin
                2633573, // Diamond Damage Skin
                2633598, // Vintage Comic Book Damage Skin
                2633729, // Stamp Damage Skin
                2633732, // Full Moon Lantern Damage Skin
                2633812, // Spooky Stencil Damage Skin
                2634019, // Chu Chu Festival Damage Skin (Unit)
                2634061, // Mistletoe Damage Skin
                2634064, // Holiday Snowman Damage Skin
                2634150, // Macaroon Damage Skin
                2634153, // Black Tiger Damage Skin
                2634176, // Slime Damage Skin
                2634250, // Fantastic Item Damage Skin
                2634262, // Wild Heart Damage Skin
                2634406, // Easter Bunny Damage Skin
                2634409, // Raindrop Damage Skin
        };
        for (int itemId : specialRewards) {
            items.add(new GachaponInfo.GachItem(itemId));
        }
        gachaponInfo.put(GachaponDlgType.SPECIAL, new GachaponInfo(Arrays.asList(messages), items));
    }

    private static void initEquipment() {
        equipments = new ArrayList<>();
        // Empress Set
        for (int i = 1152108; i <= 1152113; i++) equipments.add(new GachaponInfo.GachItem(i));// Shoulder
        for (int i = 1003172; i <= 1003176; i++) equipments.add(new GachaponInfo.GachItem(i));// Helmet
        for (int i = 1102275; i <= 1102279; i++) equipments.add(new GachaponInfo.GachItem(i));// Cape
        for (int i = 1082295; i <= 1082299; i++) equipments.add(new GachaponInfo.GachItem(i));// Gloves
        for (int i = 1052314; i <= 1052318; i++) equipments.add(new GachaponInfo.GachItem(i));// Suit
        for (int i = 1072485; i <= 1072489; i++) equipments.add(new GachaponInfo.GachItem(i));// Shoes
        equipments.add(new GachaponInfo.GachItem(1302152));// Lionheart Cutlass
        equipments.add(new GachaponInfo.GachItem(1312065));// Lionheart Champion Axe
        equipments.add(new GachaponInfo.GachItem(1322096));// Lionheart Battle Hammer
        equipments.add(new GachaponInfo.GachItem(1402095));// Lionheart Battle Scimitar
        equipments.add(new GachaponInfo.GachItem(1432086));// Lionheart Fuscina

        // Other
        equipments.add(new GachaponInfo.GachItem(3063171));
        equipments.add(new GachaponInfo.GachItem(2430307));
        equipments.add(new GachaponInfo.GachItem(1302229));
        equipments.add(new GachaponInfo.GachItem(1003176));
        equipments.add(new GachaponInfo.GachItem(1252018));
        equipments.add(new GachaponInfo.GachItem(1072489));
        equipments.add(new GachaponInfo.GachItem(3015094));
        equipments.add(new GachaponInfo.GachItem(1032110));
        equipments.add(new GachaponInfo.GachItem(1132160));
        // Hot Items
        equipments.add(new GachaponInfo.GachItem(3063231, true));
        equipments.add(new GachaponInfo.GachItem(1052315, true));
        equipments.add(new GachaponInfo.GachItem(1362022, true));
    }

    private static void initNebulite() {
        nebulites = new ArrayList<>();
        nebulites.add(new GachaponInfo.GachItem(3063231, true));
        nebulites.add(new GachaponInfo.GachItem(3063231, true));
        nebulites.add(new GachaponInfo.GachItem(3063231, true));
        for (int i = 3060000; i <= 3064490; i++) {// alot of null nebulites.. need to handle it in another way
            //nebulites.add(new GachaponInfo.GachItem(i));
        }
    }

    public static int getRandomItem(GachaponDlgType dlg) {
        GachaponInfo gachapon = gachaponInfo.get(dlg);
        if (gachapon == null) {
            return -1;
        }
        List<GachaponInfo.GachItem> rewards = gachapon.getRewards();
        if (rewards == null || rewards.size() <= 0) {
            return -1;
        }
        return rewards.get(Randomizer.nextInt(rewards.size())).getItemID();
    }

    public static GachaponDlgType getDlgByTicket(final int ticketID) {
        switch (ticketID) {
            case 5220000:
                return GachaponDlgType.TOWN;
            case 5220098:
                return GachaponDlgType.NEBULITE;
            case 5220097:
                return GachaponDlgType.CHAIR;
            case 5220099:
                return GachaponDlgType.MOUNT;
            case 5220100:
                return GachaponDlgType.SPECIAL;
            case 5451000:// Remote Gachapon Ticket TODO: handle remote gach
                return null;
        }
        return null;
    }
}
