package jhu.edu.algos.utils;

import jhu.edu.algos.lcs.LCSResult;

import java.util.List;

/**
 * This class provides functionality to estimate the constant factor 'c' in empirical complexity models.
 * Specifically, it models the number of character comparisons observed in LCS algorithms as:
 * <p>
 *   T(n) ≈ c * f(n)
 * <p>
 * where f(n) is either:
 *   - n^2  (for dynamic programming)
 *   - 2^n  (for brute force)
 * <p>
 * This estimation is done using least-squares regression over the scaling benchmark data.
 */
public class CurveFitter {

    // Threshold to prevent floating-point instability in division
    private static final double EPSILON = 1e-10;

    /**
     * Fits a power-law model T(n) ≈ c * (n^exp) to the observed comparison counts.
     * <p>
     * This is typically used to fit T(n) ≈ c * n^2 for the dynamic programming LCS algorithm.
     *
     * @param results  List of LCSResult objects from scaling tests.
     * @param exponent The exponent used in the power-law model (e.g., 2.0 for quadratic).
     * @return The best-fit constant `c` minimizing squared error.
     */
    public static double fitPowerLaw(List<LCSResult> results, double exponent) {
        double numerator = 0.0;   // Sum of T(n) * f(n)
        double denominator = 0.0; // Sum of f(n)^2

        for (LCSResult r : results) {
            // Compute average input size (n) as (|s1| + |s2|) / 2
            int n1 = r.getFirstInput().length();
            int n2 = r.getSecondInput().length();
            double avgN = (n1 + n2) / 2.0;

            // Observed comparison count from the algorithm
            long comparisons = r.getMetrics().getComparisonCount();

            // Theoretical function value: f(n) = n^exp
            double f = Math.pow(avgN, exponent);

            // Accumulate terms for linear least squares estimation
            numerator += comparisons * f;
            denominator += f * f;
        }

        // Prevent division by near-zero denominator
        return (Math.abs(denominator) < EPSILON) ? 0.0 : numerator / denominator;
    }

    /**
     * Fits an exponential model T(n) ≈ c * 2^n to the observed comparison counts.
     * <p>
     * This is used for estimating the growth of the brute-force LCS algorithm.
     *
     * @param results List of LCSResult objects from scaling tests.
     * @return The best-fit constant `c` for the exponential model.
     */
    public static double fitExponential(List<LCSResult> results) {
        double numerator = 0.0;   // Sum of T(n) * 2^n
        double denominator = 0.0; // Sum of (2^n)^2 = 4^n

        for (LCSResult r : results) {
            // Compute average input size (n) as (|s1| + |s2|) / 2
            int n1 = r.getFirstInput().length();
            int n2 = r.getSecondInput().length();
            double avgN = (n1 + n2) / 2.0;

            // Observed comparison count from the algorithm
            long comparisons = r.getMetrics().getComparisonCount();

            // Theoretical function value: f(n) = 2^n
            double f = Math.pow(2, avgN);

            // Accumulate terms for least-squares estimation
            numerator += comparisons * f;
            denominator += f * f; // Equivalent to 4^n
        }

        // Prevent division by near-zero denominator
        return (Math.abs(denominator) < EPSILON) ? 0.0 : numerator / denominator;
    }
    /**
     * Fits a linear model T(p) ≈ c * p to observed comparison counts.
     * Used for pairwise benchmarking where input length is fixed.
     *
     * @param xList List of x values (e.g., number of pairs)
     * @param yList List of y values (e.g., total comparisons)
     * @return Best-fit constant `c` in T(p) ≈ c·p
     */
    public static double fitLinear(List<Double> xList, List<Double> yList) {
        if (xList.size() != yList.size()) throw new IllegalArgumentException("x and y sizes must match");

        double numerator = 0.0;
        double denominator = 0.0;

        for (int i = 0; i < xList.size(); i++) {
            double x = xList.get(i);
            double y = yList.get(i);
            numerator += x * y;
            denominator += x * x;
        }

        return (Math.abs(denominator) < EPSILON) ? 0.0 : numerator / denominator;
    }

}
