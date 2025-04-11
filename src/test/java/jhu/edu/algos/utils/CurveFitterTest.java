package jhu.edu.algos.utils;

import jhu.edu.algos.lcs.LCSResult;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended unit test for CurveFitter.
 * Includes precise fits, noisy data, edge cases, and real-world scenarios.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CurveFitterTest {

    private final double TOLERANCE = 0.05;

    /**
     * Tests exact fit for a clean power law: T(n) = 3 * n^2
     */
    @Test
    void testQuadraticFitExact() {
        List<LCSResult> data = new ArrayList<>();
        for (int n = 5; n <= 50; n += 5) {
            data.add(mockResult(n, (long) (3 * Math.pow(n, 2))));
        }
        double c = CurveFitter.fitPowerLaw(data, 2.0);
        assertEquals(3.0, c, TOLERANCE, "Should fit clean 3n² curve");
    }

    /**
     * Tests a non-integer exponent model: T(n) = 2.5 * n^2.5
     */
    @Test
    void testFractionalExponent() {
        List<LCSResult> data = new ArrayList<>();
        for (int n = 5; n <= 50; n += 5) {
            data.add(mockResult(n, (long) (2.5 * Math.pow(n, 2.5))));
        }
        double c = CurveFitter.fitPowerLaw(data, 2.5);
        assertEquals(2.5, c, TOLERANCE, "Should fit fractional power law (n^2.5)");
    }

    /**
     * Tests exponential model with clean data: T(n) = 1.5 * 2^n
     */
    @Test
    void testExponentialExact() {
        List<LCSResult> data = new ArrayList<>();
        for (int n = 5; n <= 15; n++) {
            data.add(mockResult(n, (long) (1.5 * Math.pow(2, n))));
        }
        double c = CurveFitter.fitExponential(data);
        assertEquals(1.5, c, 0.1, "Should fit 1.5·2ⁿ model");
    }

    /**
     * Tests power-law with small random noise
     */
    @Test
    void testPowerLawWithNoise() {
        List<LCSResult> data = new ArrayList<>();
        Random rand = new Random(42);

        for (int n = 10; n <= 60; n += 5) {
            double clean = 2.0 * Math.pow(n, 2);
            double noise = clean + rand.nextGaussian() * 0.1 * clean;
            data.add(mockResult(n, (long) noise));
        }

        double c = CurveFitter.fitPowerLaw(data, 2.0);
        assertTrue(Math.abs(c - 2.0) < 0.2, "Should approximate 2.0 with minor noise");
    }

    /**
     * Tests fitting for very small n values (1–3)
     */
    @Test
    void testTinyNValues() {
        List<LCSResult> data = new ArrayList<>();
        for (int n = 1; n <= 3; n++) {
            data.add(mockResult(n, (long) (4 * Math.pow(n, 2))));
        }

        double c = CurveFitter.fitPowerLaw(data, 2.0);
        assertEquals(4.0, c, 0.2, "Should still fit small n correctly");
    }

    /**
     * Tests with mismatched string lengths (s1 ≠ s2)
     */
    @Test
    void testUnevenStringLengths() {
        List<LCSResult> data = new ArrayList<>();

        data.add(new LCSResult("S1 vs S2", "A".repeat(10), "B".repeat(30), "", metrics(360))); // avg = 20
        data.add(new LCSResult("S2 vs S3", "A".repeat(15), "C".repeat(25), "", metrics(400))); // avg = 20

        double c = CurveFitter.fitPowerLaw(data, 2.0);
        assertTrue(c > 0, "Should handle uneven inputs based on average size");
    }

    /**
     * Ensures empty input doesn't throw and returns 0.
     */
    @Test
    void testEmptyInput() {
        List<LCSResult> empty = new ArrayList<>();
        assertEquals(0.0, CurveFitter.fitPowerLaw(empty, 2.0));
        assertEquals(0.0, CurveFitter.fitExponential(empty));
    }

    // === Helper Methods ===

    /**
     * Builds an LCSResult with equal-length s1/s2 and custom comparison count.
     */
    private LCSResult mockResult(int avgLen, long comparisons) {
        String s1 = "A".repeat(avgLen);
        String s2 = "T".repeat(avgLen);
        PerformanceMetrics m = new PerformanceMetrics();
        m.addComparisons(comparisons);
        return new LCSResult("Synthetic", s1, s2, "", m);
    }

    /**
     * Quickly builds a PerformanceMetrics with comparison count.
     */
    private PerformanceMetrics metrics(long comparisons) {
        PerformanceMetrics m = new PerformanceMetrics();
        m.addComparisons(comparisons);
        return m;
    }
    /**
     * Tests linear fit for pairwise benchmark data: T(p) = 7 * p
     */
    @Test
    void testLinearFitExact() {
        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();

        for (int p = 1; p <= 10; p++) {
            x.add((double) p);
            y.add(7.0 * p);
        }

        double c = CurveFitter.fitLinear(x, y);
        assertEquals(7.0, c, TOLERANCE, "Should fit clean linear T(p) ≈ 7·p model");
    }

    /**
     * Tests linear fit with noisy pairwise data: T(p) ≈ 10p + noise
     */
    @Test
    void testLinearFitWithNoise() {
        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();
        Random rand = new Random(42);

        for (int p = 1; p <= 15; p++) {
            x.add((double) p);
            double noise = 10.0 * p + rand.nextGaussian() * 2.0;  // 20% noise
            y.add(noise);
        }

        double c = CurveFitter.fitLinear(x, y);
        assertTrue(Math.abs(c - 10.0) < 2.0, "Should approximate 10.0 with noise");
    }
}
