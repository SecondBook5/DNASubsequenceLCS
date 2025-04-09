package jhu.edu.algos.lcs;

import jhu.edu.algos.utils.PerformanceMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements the Longest Common Subsequence (LCS) algorithm
 * using a brute-force powerset approach.
 *
 * This class should only be used for very short sequences due to its exponential runtime.
 */
public class LCSBruteForce extends AbstractLCS {

    /**
     * Computes the longest common subsequence (LCS) by generating
     * all subsequences of the shorter string and testing which
     * are subsequences of the longer string.
     *
     * @param label A label describing the comparison (e.g., "S1 vs S2").
     * @param s1    First input string.
     * @param s2    Second input string.
     * @return An LCSResult object with the longest matching subsequence and metrics.
     */
    @Override
    public LCSResult computeLCS(String label, String s1, String s2) {
        // Start timing the brute-force operation
        metrics.startTimer();

        // Handle null input safely
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("Input strings cannot be null.");
        }

        // Identify which string is shorter to reduce search space
        String shorter = s1.length() <= s2.length() ? s1 : s2;
        String longer = s1.length() > s2.length() ? s1 : s2;

        // Generate all subsequences of the shorter string
        List<String> allSubseq = generateAllSubsequences(shorter);

        // Sort by descending length so we find longest valid one first
        allSubseq.sort((a, b) -> Integer.compare(b.length(), a.length()));

        // Track the longest valid subsequence
        String bestLCS = "";

        for (String candidate : allSubseq) {
            // Check if candidate is a subsequence of the longer string
            if (isSubsequence(candidate, longer)) {
                bestLCS = candidate;
                break; // Found the longest valid LCS, no need to check shorter ones
            }
        }

        // Stop timing
        metrics.stopTimer();

        // Return structured result
        return new LCSResult(label, s1, s2, bestLCS, metrics);
    }

    /**
     * Recursively generates all subsequences of a string.
     *
     * @param input The input string to generate subsequences for.
     * @return A list containing all subsequences.
     */
    private List<String> generateAllSubsequences(String input) {
        List<String> subsequences = new ArrayList<>();
        generateSubsequencesHelper(input, 0, new StringBuilder(), subsequences);
        return subsequences;
    }

    /**
     * Recursive helper to generate all subsequences.
     *
     * @param input        The original string.
     * @param index        The current index being considered.
     * @param current      The current subsequence being built.
     * @param resultHolder The list that accumulates all subsequences.
     */
    private void generateSubsequencesHelper(String input, int index, StringBuilder current, List<String> resultHolder) {
        // Base case: we've considered all characters
        if (index == input.length()) {
            resultHolder.add(current.toString());
            return;
        }

        // Include the current character
        current.append(input.charAt(index));
        generateSubsequencesHelper(input, index + 1, current, resultHolder);
        current.deleteCharAt(current.length() - 1); // Backtrack

        // Exclude the current character
        generateSubsequencesHelper(input, index + 1, current, resultHolder);
    }

    /**
     * Checks whether one string is a subsequence of another.
     * Updates metrics with each character comparison.
     *
     * @param subseq  The candidate subsequence.
     * @param fullStr The full string to test against.
     * @return true if subseq is a valid subsequence of fullStr.
     */
    private boolean isSubsequence(String subseq, String fullStr) {
        int i = 0; // pointer for subseq
        int j = 0; // pointer for fullStr

        while (i < subseq.length() && j < fullStr.length()) {
            metrics.incrementComparisonCount(); // Track every character comparison

            if (subseq.charAt(i) == fullStr.charAt(j)) {
                i++;
            }
            j++;
        }

        return i == subseq.length();
    }
}
