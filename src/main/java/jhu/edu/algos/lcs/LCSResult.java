package jhu.edu.algos.lcs;

import jhu.edu.algos.utils.PerformanceMetrics;

/**
 * Data structure representing the result of a Longest Common Subsequence (LCS) computation.
 * Stores the LCS string, its length, performance metrics, and optional DP matrix.
 */
public class LCSResult {

    // The input label or identifier (e.g., "S1 vs S2")
    private final String comparisonLabel;

    // The computed LCS string
    private final String lcs;

    // The first input string
    private final String s1;

    // The second input string
    private final String s2;

    // Metrics object containing time and comparison count
    private final PerformanceMetrics metrics;

    // Optional: the dynamic programming matrix used in LCSDynamic
    private final int[][] dpMatrix;

    /**
     * Constructs a result object for an LCS run (without DP matrix).
     *
     * @param comparisonLabel A label describing the comparison (e.g., "S1 vs S2").
     * @param s1              First input string.
     * @param s2              Second input string.
     * @param lcs             The LCS string result.
     * @param metrics         Performance metrics collected during execution.
     */
    public LCSResult(String comparisonLabel, String s1, String s2, String lcs, PerformanceMetrics metrics) {
        this(comparisonLabel, s1, s2, lcs, metrics, null);
    }

    /**
     * Constructs a result object for an LCS run (with DP matrix).
     *
     * @param comparisonLabel A label describing the comparison (e.g., "S1 vs S2").
     * @param s1              First input string.
     * @param s2              Second input string.
     * @param lcs             The LCS string result.
     * @param metrics         Performance metrics collected during execution.
     * @param dpMatrix        Optional dynamic programming matrix (may be null).
     */
    public LCSResult(String comparisonLabel, String s1, String s2, String lcs,
                     PerformanceMetrics metrics, int[][] dpMatrix) {
        this.comparisonLabel = comparisonLabel;
        this.s1 = s1;
        this.s2 = s2;
        this.lcs = lcs;
        this.metrics = metrics;
        this.dpMatrix = dpMatrix;
    }

    public String getComparisonLabel() {
        return comparisonLabel;
    }

    public String getLCS() {
        return lcs;
    }

    public int getLCSLength() {
        return lcs.length();
    }

    public String getFirstInput() {
        return s1;
    }

    public String getSecondInput() {
        return s2;
    }

    public PerformanceMetrics getMetrics() {
        return metrics;
    }

    /**
     * Returns the DP matrix used in the dynamic programming computation,
     * or null if the algorithm did not generate one.
     *
     * @return 2D int matrix of LCS scores, or null if unavailable.
     */
    public int[][] getDpMatrix() {
        return dpMatrix;
    }

    @Override
    public String toString() {
        return String.format(
                """
                === LCS Result: %s ===
                Input 1: %s
                Input 2: %s
                LCS    : %s
                Length : %d
                Comparisons: %d
                Time (ms)  : %d
                """,
                comparisonLabel,
                s1,
                s2,
                lcs,
                getLCSLength(),
                metrics.getComparisonCount(),
                metrics.getElapsedTimeMs()
        );
    }
}
