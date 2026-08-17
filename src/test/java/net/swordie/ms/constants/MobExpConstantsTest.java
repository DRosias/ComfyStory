package net.swordie.ms.constants;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MobExpConstantsTest {
    private static final double RATE_TOLERANCE = 0.000000000001D;

    private static final Map<Integer, Double> CHECKPOINTS = Map.ofEntries(
        Map.entry(30, 0.34D),
        Map.entry(60, 0.79D),
        Map.entry(100, 1.42D),
        Map.entry(140, 4.11D),
        Map.entry(170, 4.7D),
        Map.entry(190, 5.84D),
        Map.entry(199, 7.26D),
        Map.entry(200, 10.74D),
        Map.entry(205, 14.31D),
        Map.entry(209, 11.68D),
        Map.entry(210, 18.58D),
        Map.entry(215, 27.2D),
        Map.entry(220, 48.31D),
        Map.entry(225, 67.24D),
        Map.entry(230, 112.83D),
        Map.entry(235, 157.4D),
        Map.entry(240, 270.55D),
        Map.entry(245, 378.25D),
        Map.entry(250, 855.81D),
        Map.entry(255, 713.55D),
        Map.entry(259, 721.35D),
        Map.entry(260, 586.99D),
        Map.entry(270, 1009D),
        Map.entry(275, 2054D),
        Map.entry(280, 5841D),
        Map.entry(285, 16634D),
        Map.entry(290, 47437D),
        Map.entry(295, 134940D),
        Map.entry(299, 261426D)
    );

    @Test
    public void earlyLevelsUseRequestedRates() {
        for (int level = 1; level <= 9; level++) {
            assertEquals(1D, MobExpConstants.getRateForCharacterLevel(level), 0D);
        }
        for (int level = 10; level <= 30; level++) {
            assertEquals(0.34D, MobExpConstants.getRateForCharacterLevel(level), 0D);
        }
    }

    @Test
    public void researchedCheckpointsAreExact() {
        CHECKPOINTS.forEach((level, rate) ->
            assertEquals(rate, MobExpConstants.getRateForCharacterLevel(level), 0D)
        );
    }

    @Test
    public void intermediateRatesUseGeometricInterpolation() {
        assertEquals(
            interpolate(30, 0.34D, 60, 0.79D, 45),
            MobExpConstants.getRateForCharacterLevel(45),
            RATE_TOLERANCE
        );
        assertEquals(
            interpolate(205, 14.31D, 209, 11.68D, 207),
            MobExpConstants.getRateForCharacterLevel(207),
            RATE_TOLERANCE
        );
        assertEquals(
            interpolate(295, 134940D, 299, 261426D, 297),
            MobExpConstants.getRateForCharacterLevel(297),
            RATE_TOLERANCE
        );
    }

    @Test
    public void decreasingCheckpointRangesRemainDecreasing() {
        assertStrictlyDecreasing(205, 209);
        assertStrictlyDecreasing(250, 255);
        assertStrictlyDecreasing(259, 260);
    }

    @Test
    public void allExpEarningLevelsHavePositiveFiniteRates() {
        for (int level = 1; level <= 299; level++) {
            double rate = MobExpConstants.getRateForCharacterLevel(level);
            assertTrue("Expected a positive rate at level " + level, rate > 0D);
            assertTrue("Expected a finite rate at level " + level, Double.isFinite(rate));
        }
    }

    @Test
    public void maxLevelDoesNotReuseLevel299Rate() {
        assertEquals(261426D, MobExpConstants.getRateForCharacterLevel(299), 0D);
        assertEquals(0D, MobExpConstants.getRateForCharacterLevel(300), 0D);
        assertEquals(0D, MobExpConstants.getRateForCharacterLevel(301), 0D);
    }

    @Test
    public void fractionalAndLargeScalingUseLongExpSafely() {
        assertEquals(34L, MobExpConstants.scaleExpSafely(100L, 0.34D));
        assertEquals(
            (long) (2_582_906L * 261426D),
            MobExpConstants.scaleExpSafely(2_582_906L, 261426D)
        );
        assertEquals(Long.MAX_VALUE, MobExpConstants.scaleExpSafely(Long.MAX_VALUE, 2D));
        assertEquals(Long.MAX_VALUE, MobExpConstants.addExpSafely(Long.MAX_VALUE - 5, 10));
    }

    private static double interpolate(int startLevel, double startRate, int endLevel, double endRate, int level) {
        double progress = (level - startLevel) / (double) (endLevel - startLevel);
        return Math.exp(Math.log(startRate) + progress * (Math.log(endRate) - Math.log(startRate)));
    }

    private static void assertStrictlyDecreasing(int startLevel, int endLevel) {
        double previous = MobExpConstants.getRateForCharacterLevel(startLevel);
        for (int level = startLevel + 1; level <= endLevel; level++) {
            double current = MobExpConstants.getRateForCharacterLevel(level);
            assertTrue("Expected level " + level + " rate to decrease", current < previous);
            previous = current;
        }
    }
}
