package jhu.edu.algos.lcs;

import jhu.edu.algos.utils.PerformanceMetrics;

/**
 * Implements the Longest Common Subsequence (LCS) algorithm
 * using a recursive brute-force approach.
 * Avoids memory blowup by searching subsequences without storing all of them.
 */
public class LCSBruteForce extends AbstractLCS {

    // Track the best result found during backtracking
    private String bestLCS;

    /**
     * Computes the longest common subsequence (LCS) by recursively
     * exploring all subsequences of the shorter string, but avoids
     * storing them in memory. Each candidate is checked against the
     * longer string to see if it's a valid subsequence.
     *
     * @param label A label describing the comparison (e.g., "S1 vs S2").
     * @param s1    First input string.
     * @param s2    Second input string.
     * @return An LCSResult object with the longest matching subsequence and metrics.
     */
    @Override
    public LCSResult computeLCS(String label, String s1, String s2) {
        // Start tracking time
        metrics.startTimer();

        // Validate input: null or empty strings are not allowed
        if (s1 == null || s2 == null || s1.isEmpty() || s2.isEmpty()) {
            throw new IllegalArgumentException("Input sequences must not be null or empty.");
        }

        // Choose the shorter string to reduce the search space
        String shorter = s1.length() <= s2.length() ? s1 : s2;
        String longer = s1.length() > s2.length() ? s1 : s2;

        // Estimate memory:
        // - StringBuilder buffer for current subsequence
        // - O(2^n) recursive calls × estimated frame size
        int n = shorter.length();
        long candidateBufferBytes = 2L * n * Character.BYTES;
        long estimatedStackBytes = (long) Math.pow(2, n) * 64; // ~64 bytes per stack frame
        metrics.setEstimatedSpaceBytes(candidateBufferBytes + estimatedStackBytes);

        // Reset best result
        bestLCS = "";

        // Begin DFS-style backtracking
        exploreSubsequences(shorter, longer, 0, new StringBuilder());

        // Stop the timer
        metrics.stopTimer();

        // Return results
        return new LCSResult(label, s1, s2, bestLCS, metrics);
    }

    /**
     * Recursive method to explore all possible subsequences of the shorter string.
     * For each candidate, it checks if it's a valid subsequence of the longer string.
     *
     * @param shorter The string being explored.
     * @param longer  The string checked against.
     * @param index   Current index in the shorter string.
     * @param current The current subsequence being built.
     */
    private void exploreSubsequences(String shorter, String longer, int index, StringBuilder current) {
        // If we've considered all characters, check the built string
        if (index == shorter.length()) {
            if (current.length() > bestLCS.length() && isSubsequence(current, longer)) {
                bestLCS = current.toString();
            }
            return;
        }

        // Include the current character
        current.append(shorter.charAt(index));
        exploreSubsequences(shorter, longer, index + 1, current);
        current.deleteCharAt(current.length() - 1); // backtrack

        // Exclude the current character
        exploreSubsequences(shorter, longer, index + 1, current);
    }

    /**
     * Checks whether a candidate subsequence exists within the full string.
     * Increments the comparison counter for each character check.
     *
     * @param candidate The subsequence candidate.
     * @param fullStr   The full reference string.
     * @return true if the candidate is a valid subsequence of fullStr.
     */
    private boolean isSubsequence(StringBuilder candidate, String fullStr) {
        int i = 0;
        int j = 0;

        while (i < candidate.length() && j < fullStr.length()) {
            metrics.incrementComparisonCount();
            if (candidate.charAt(i) == fullStr.charAt(j)) {
                i++;
            }
            j++;
        }

        return i == candidate.length();
    }
}
