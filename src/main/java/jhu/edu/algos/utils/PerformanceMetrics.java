package jhu.edu.algos.utils;

/**
 * A utility class to track performance metrics for sequence alignment algorithms
 * such as the Longest Common Subsequence (LCS). It measures both the number of
 * character comparisons and the total elapsed execution time in milliseconds.
 *
 */
public class PerformanceMetrics {

    // Stores the system time (in nanoseconds) when the operation begins
    private long startTime;

    // Stores the system time (in nanoseconds) when the operation ends
    private long endTime;

    // Tracks the total number of character comparisons performed
    private long comparisonCount;

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
    }

    /**
     * Starts the timer by recording the current system time in nanoseconds.
     *
     * This must be called before any computation begins.
     */
    public void startTimer() {
        // Capture current time in nanoseconds as the start
        this.startTime = System.nanoTime();
    }

    /**
     * Stops the timer by recording the current system time in nanoseconds.
     *
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
     * Increments the character comparison counter by one.
     *
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
     * Retrieves the elapsed time in milliseconds between the last startTimer() and stopTimer() calls.
     *
     * @return the time in milliseconds; returns 0 if timing is not valid
     */
    public long getElapsedTimeMs() {
        // Return 0 if timer hasn't been started or stopped
        if (startTime == 0 || endTime == 0) {
            return 0;
        }

        // Compute the elapsed time in milliseconds
        return (endTime - startTime) / 1_000_000;
    }

    /**
     * Resets only the comparison count to zero.
     *
     * Useful if you want to reuse the timer but reset counting for a new operation.
     */
    public void resetComparisonCount() {
        // Reset the comparison counter to 0
        this.comparisonCount = 0;
    }

    /**
     * Resets all recorded metrics including timers and comparison count.
     *
     * Allows the object to be reused from a clean state.
     */
    public void resetAll() {
        // Reset all tracked metrics and timers
        this.startTime = 0;
        this.endTime = 0;
        this.comparisonCount = 0;
    }
}
