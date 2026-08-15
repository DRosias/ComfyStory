package net.swordie.ms;

import net.swordie.ms.enums.WorldId;

/**
 * Created on 2/18/2017.
 */
public class ServerConfig {

    public static final int USER_LIMIT = 500;
    public static final WorldId WORLD_ID = WorldId.Bera;
    public static final String SERVER_NAME = "Bera";
    public static final String EVENT_MSG = String.format("#bSwordie#k v%d.%s\r\nPlayers online: #b", ServerConstants.VERSION, ServerConstants.MINOR_VERSION);
    public static final String RECOMMEND_MSG = "I recommend this world to you.";
    public static final int MAX_CHARACTERS = 30;
    public static final String HEAP_DUMP_DIR = "../heapdumps";
    public static final int CASH_REWARD_MULTIPLIER = 2;

    public static int applyCashRewardMultiplier(int amount) {
        if (amount <= 0) {
            return amount;
        }
        return (int) Math.min((long) amount * CASH_REWARD_MULTIPLIER, Integer.MAX_VALUE);
    }

}
