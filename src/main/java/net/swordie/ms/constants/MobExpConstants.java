package net.swordie.ms.constants;

import java.util.Arrays;
import java.util.List;

public class MobExpConstants {
    private static final int FIRST_LEVEL = 1;
    private static final int LAST_EXP_EARNING_LEVEL = 299;
    private static final int EARLY_RATE_END_LEVEL = 9;
    private static final int EARLY_BASELINE_END_LEVEL = 29;
    private static final double EARLY_RATE = 1D;
    private static final double EARLY_BASELINE_RATE = 0.34D;

    /**
     * v0.1 mob EXP checkpoints calibrated around gently increasing target minutes per level.
     * Notable targets are about 6 minutes at 30, 7 at 100, 8 at 200, 8.5 at 210,
     * 10.5 at 250, 12.5 at 275, 14 at 290, and 15 at 299. Fresh-account pacing
     * targets are about 2-3 hours to 30, 24-25 hours to 210, and 40-45 hours to 300.
     *
     * Rates may decrease when better training content becomes available. Values between
     * checkpoints are generated once with geometric (log-linear) interpolation.
     */
    private static final List<MobExpRate> MOB_EXP_RATE_CHECKPOINTS = List.of(
        new MobExpRate(30, 0.34D),
        new MobExpRate(60, 0.79D),
        new MobExpRate(100, 1.42D),
        new MobExpRate(140, 4.11D),
        new MobExpRate(170, 4.7D),
        new MobExpRate(190, 5.84D),
        new MobExpRate(199, 7.26D),
        new MobExpRate(200, 10.74D),
        new MobExpRate(205, 14.31D),
        new MobExpRate(209, 11.68D),
        new MobExpRate(210, 18.58D),
        new MobExpRate(215, 27.2D),
        new MobExpRate(220, 48.31D),
        new MobExpRate(225, 67.24D),
        new MobExpRate(230, 112.83D),
        new MobExpRate(235, 157.4D),
        new MobExpRate(240, 270.55D),
        new MobExpRate(245, 378.25D),
        new MobExpRate(250, 855.81D),
        new MobExpRate(255, 713.55D),
        new MobExpRate(259, 721.35D),
        new MobExpRate(260, 586.99D),
        new MobExpRate(270, 1009D),
        new MobExpRate(275, 2054D),
        new MobExpRate(280, 5841D),
        new MobExpRate(285, 16634D),
        new MobExpRate(290, 47437D),
        new MobExpRate(295, 134940D),
        new MobExpRate(299, 261426D)
    );

    private static final double[] MOB_EXP_RATE_BY_LEVEL = buildLevelRateTable();

    public record MobExpRate(
        int level,
        double rate
    ) {}

    private static double[] buildLevelRateTable() {
        double[] rates = new double[LAST_EXP_EARNING_LEVEL + 1];
        Arrays.fill(rates, FIRST_LEVEL, EARLY_RATE_END_LEVEL + 1, EARLY_RATE);
        Arrays.fill(rates, EARLY_RATE_END_LEVEL + 1, EARLY_BASELINE_END_LEVEL + 1, EARLY_BASELINE_RATE);

        for (int i = 0; i < MOB_EXP_RATE_CHECKPOINTS.size() - 1; i++) {
            MobExpRate start = MOB_EXP_RATE_CHECKPOINTS.get(i);
            MobExpRate end = MOB_EXP_RATE_CHECKPOINTS.get(i + 1);
            rates[start.level()] = start.rate();

            double logStart = Math.log(start.rate());
            double logEnd = Math.log(end.rate());
            int levelSpan = end.level() - start.level();
            for (int level = start.level() + 1; level < end.level(); level++) {
                double progress = (level - start.level()) / (double) levelSpan;
                rates[level] = Math.exp(logStart + progress * (logEnd - logStart));
            }
            rates[end.level()] = end.rate();
        }

        return rates;
    }

    /**
     * Returns the baseline mob EXP multiplier for a character level. Max-level characters
     * explicitly receive no mob EXP rather than inheriting level 299's multiplier.
     */
    public static double getRateForCharacterLevel(int level) {
        if (level > LAST_EXP_EARNING_LEVEL) {
            return 0D;
        }
        return MOB_EXP_RATE_BY_LEVEL[Math.max(level, FIRST_LEVEL)];
    }

    /**
     * Scales a positive EXP value without allowing a floating-point conversion to overflow.
     * Fractional EXP is truncated because character EXP is stored as a long.
     */
    public static long scaleExpSafely(long exp, double multiplier) {
        if (exp <= 0 || multiplier <= 0 || Double.isNaN(multiplier)) {
            return 0L;
        }

        double scaledExp = exp * multiplier;
        if (!Double.isFinite(scaledExp) || scaledExp >= Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        return (long) scaledExp;
    }

    /**
     * Adds non-negative EXP values without allowing overflow to wrap the result.
     */
    public static long addExpSafely(long exp, long bonusExp) {
        if (bonusExp <= 0) {
            return exp;
        }
        if (exp >= Long.MAX_VALUE - bonusExp) {
            return Long.MAX_VALUE;
        }
        return exp + bonusExp;
    }
}
