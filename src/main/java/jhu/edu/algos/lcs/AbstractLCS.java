package jhu.edu.algos.lcs;

import jhu.edu.algos.utils.PerformanceMetrics;

/**
 * Abstract base class for all Longest Common Subsequence (LCS) algorithms.
 * Provides a standard interface for performance tracking and enforces the
 * implementation of a computeLCS method returning full result information.
 */
public abstract class AbstractLCS {

    // Performance tracker for timing and comparison counting
    protected final PerformanceMetrics metrics;

    /**
     * Default constructor initializes a fresh PerformanceMetrics tracker
     * for subclass use during LCS computation.
     */
    public AbstractLCS() {
        // Create a fresh performance tracker
        this.metrics = new PerformanceMetrics();
    }

    /**
     * Returns the performance metrics associated with the most recent computation.
     *
     * @return PerformanceMetrics object containing runtime and comparison count.
     */
    public PerformanceMetrics getMetrics() {
        // Return the internal tracker
        return this.metrics;
    }

    /**
     * Computes the longest common subsequence between two input strings and returns
     * both the LCS and associated performance metrics.
     *
     * @param label A descriptive label for this comparison (e.g., "S1 vs S2").
     * @param s1    The first input string.
     * @param s2    The second input string.
     * @return An LCSResult object containing the LCS string and performance metrics.
     */
    public abstract LCSResult computeLCS(String label, String s1, String s2);
}
