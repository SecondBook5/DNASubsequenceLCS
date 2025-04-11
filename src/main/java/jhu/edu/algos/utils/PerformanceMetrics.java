package jhu.edu.algos.utils;

/**
 * A utility class to track performance metrics for sequence alignment algorithms
 * such as the Longest Common Subsequence (LCS). It tracks:
 * - The number of character comparisons
 * - Total execution time in milliseconds
 * - Estimated space usage in bytes
 */
public class PerformanceMetrics {

    // Time tracking
    private long startTime;

    // Stores the system time (in nanoseconds) when the operation ends
    private long endTime;

    // Comparison counter
    private long comparisonCount;

    // Estimated space usage in bytes
    private long estimatedSpaceBytes;

    /**
     * Default constructor initializes all performance counters and timers to zero.
     */
    public PerformanceMetrics() {
        // No start time has been set yet
        this.startTime = 0;
        // No end time has been recorded yet
        this.endTime = 0;
        // No comparisons have been counted yet
        this.comparisonCount = 0;
        this.estimatedSpaceBytes = 0;
    }

    /**
     * Starts the timer by recording the current system time in nanoseconds.
     * This must be called before any computation begins.
     */
    public void startTimer() {
        // Capture current time in nanoseconds as the start
        this.startTime = System.nanoTime();
    }

    /**
     * Stops the timer by recording the current system time in nanoseconds.
     * This must be called after the computation is complete.
     * Throws an exception if startTimer() was never called.
     */
    public void stopTimer() {
        // Ensure that startTimer() was invoked before stopping the timer
        if (startTime == 0) {
            throw new IllegalStateException("Error: stopTimer() called before startTimer().");
        }

        // Capture current time in nanoseconds as the end
        this.endTime = System.nanoTime();
    }

    /**
     * Retrieves the elapsed time in milliseconds between the last startTimer() and stopTimer() calls.
     *
     * @return the time in milliseconds; returns 0 if timing is not valid
     */
    public long getElapsedTimeMs() {
        if (startTime == 0 || endTime == 0) {
            return 0;
        }
        return (endTime - startTime) / 1_000_000;
    }

    /**
     * Retrieves the elapsed time in seconds (for scientific output).
     *
     * @return time in seconds as double
     */
    public double getElapsedTimeSeconds() {
        return getElapsedTimeMs() / 1000.0;
    }

    /**
     * Increments the character comparison counter by one.
     * This should be called every time a character comparison (e.g., A[i] == B[j]) is made.
     */
    public void incrementComparisonCount() {
        // Increment the comparison counter by one
        this.comparisonCount++;
    }

    /**
     * Adds a specified number of comparisons to the total count.
     *
     * @param amount the number of comparisons to add
     * @throws IllegalArgumentException if the amount is negative
     */
    public void addComparisons(long amount) {
        // Validate that the input amount is non-negative
        if (amount < 0) {
            throw new IllegalArgumentException("Comparison count cannot be negative.");
        }

        // Add the specified number of comparisons to the total count
        this.comparisonCount += amount;
    }

    /**
     * Retrieves the total number of character comparisons recorded.
     *
     * @return the total number of comparisons
     */
    public long getComparisonCount() {
        // Return the recorded comparison count
        return this.comparisonCount;
    }

    /**
     * Sets the estimated space usage in bytes.
     *
     * @param bytes estimated space used (must be non-negative)
     */
    public void setEstimatedSpaceBytes(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("Space used cannot be negative.");
        }
        this.estimatedSpaceBytes = bytes;
    }

    /**
     * Retrieves the estimated space usage in bytes.
     *
     * @return estimated space usage
     */
    public long getEstimatedSpaceBytes() {
        return this.estimatedSpaceBytes;
    }

    /**
     * Retrieves the estimated space usage in kilobytes.
     *
     * @return estimated space in KB
     */
    public double getEstimatedSpaceKB() {
        return estimatedSpaceBytes / 1024.0;
    }

    /**
     * Retrieves the estimated space usage in megabytes.
     *
     * @return estimated space in MB
     */
    public double getEstimatedSpaceMB() {
        return estimatedSpaceBytes / (1024.0 * 1024.0);
    }

    /**
     * Resets only the comparison count to zero.
     */
    public void resetComparisonCount() {
        // Reset the comparison counter to 0
        this.comparisonCount = 0;
    }

    /**
     * Resets all recorded metrics including timers, comparisons, and space.
     */
    public void resetAll() {
        // Reset all tracked metrics and timers
        this.startTime = 0;
        this.endTime = 0;
        this.comparisonCount = 0;
        this.estimatedSpaceBytes = 0;
    }
}
