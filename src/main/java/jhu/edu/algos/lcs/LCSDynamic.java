package jhu.edu.algos.lcs;

import jhu.edu.algos.utils.PerformanceMetrics;

/**
 * Implements the Longest Common Subsequence (LCS) algorithm using
 * a bottom-up dynamic programming approach.
 * This class tracks performance metrics including character comparisons and runtime.
 */
public class LCSDynamic extends AbstractLCS {

    // Optional flag to toggle DP matrix printing
    private boolean printMatrix = false;

    /**
     * Enables matrix printing for debugging or educational output.
     */
    public void enableMatrixPrinting() {
        this.printMatrix = true;
    }

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

        // Validate input: null or empty strings are not allowed
        if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty()) {
            throw new IllegalArgumentException("Input sequences must not be null or empty.");
        }

        // Determine lengths of both strings
        int m = s1.length();
        int n = s2.length();

        // Create the dynamic programming matrix (m+1) x (n+1)
        int[][] dp = new int[m + 1][n + 1];

        // Record estimated space usage in bytes (each int is 4 bytes)
        metrics.setEstimatedSpaceBytes((long)(m + 1) * (n + 1) * Integer.BYTES);

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

        // Optionally print the full DP matrix
        if (printMatrix) {
            printSubsequenceMatrix(s1, s2, dp);
        }

        // New version that includes the DP matrix in the result
        return new LCSResult(label, s1, s2, lcs.toString(), metrics, dp);

    }

    /**
     * Prints the DP matrix used to compute the LCS.
     *
     * @param s1 First sequence
     * @param s2 Second sequence
     * @param dp DP table built by the algorithm
     */
    private void printSubsequenceMatrix(String s1, String s2, int[][] dp) {
        System.out.println("\nPrinting out subsequence matrix...");

        // Print header row
        System.out.print("    ");
        for (int j = 0; j < s2.length(); j++) {
            System.out.print(" " + s2.charAt(j));
        }
        System.out.println();

        for (int i = 0; i <= s1.length(); i++) {
            if (i == 0) {
                System.out.print("  ");
            } else {
                System.out.print(s1.charAt(i - 1) + " ");
            }

            for (int j = 0; j <= s2.length(); j++) {
                System.out.print(dp[i][j] + " ");
            }
            System.out.println();
        }
    }
}
