package jhu.edu.algos.lcs;

import jhu.edu.algos.utils.PerformanceMetrics;

/**
 * Implements the Longest Common Subsequence (LCS) algorithm using
 * a bottom-up dynamic programming approach.
 * This class tracks performance metrics including character comparisons and runtime.
 */
public class LCSDynamic extends AbstractLCS {

    /**
     * Computes the longest common subsequence (LCS) between two input strings
     * using the classic dynamic programming approach.
     *
     * @param label A label representing the comparison (e.g., "S1 vs S2").
     * @param s1    The first input string.
     * @param s2    The second input string.
     * @return An LCSResult object containing the LCS string, input details, and performance metrics.
     */
    public LCSResult computeLCS(String label, String s1, String s2) {
        // Start the timer for performance tracking
        metrics.startTimer();

        // Handle null input defensively
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("Input strings cannot be null.");
        }

        // Determine lengths of both strings
        int m = s1.length();
        int n = s2.length();

        // Create the dynamic programming matrix (m+1) x (n+1)
        int[][] dp = new int[m + 1][n + 1];

        // Fill the matrix using bottom-up approach
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {

                // Track each character comparison
                metrics.incrementComparisonCount();

                // Check character match condition
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // Reconstruct the LCS from the dp matrix
        StringBuilder lcs = new StringBuilder();
        int i = m, j = n;

        while (i > 0 && j > 0) {
            // Count this check too
            metrics.incrementComparisonCount();

            // If characters match, include in LCS
            if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                lcs.insert(0, s1.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }

        // Stop performance timer
        metrics.stopTimer();

        // Create and return the result object
        return new LCSResult(label, s1, s2, lcs.toString(), metrics);
    }
}
