package jhu.edu.algos.utils;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for PerformanceMetrics.
 * Verifies the correctness of timer logic, comparison counting,
 * and reset functionality.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PerformanceMetricsTest {

    private PerformanceMetrics metrics;

    @BeforeEach
    void setUp() {
        // Initialize a new PerformanceMetrics object before each test
        metrics = new PerformanceMetrics();
    }

    @Test
    void testStartAndStopTimer() {
        // Start the timer
        metrics.startTimer();

        // Simulate a small delay
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            fail("Sleep interrupted during test.");
        }

        // Stop the timer
        metrics.stopTimer();

        // Elapsed time should be > 0
        long elapsed = metrics.getElapsedTimeMs();
        assertTrue(elapsed >= 10, "Elapsed time should be at least 10 ms.");
    }

    @Test
    void testStopTimerWithoutStart() {
        // Expect error if stop is called without starting
        IllegalStateException thrown = assertThrows(IllegalStateException.class, metrics::stopTimer);
        assertEquals("Error: stopTimer() called before startTimer().", thrown.getMessage());
    }

    @Test
    void testIncrementComparisonCount() {
        // Increment 3 times
        metrics.incrementComparisonCount();
        metrics.incrementComparisonCount();
        metrics.incrementComparisonCount();

        // Check count is 3
        assertEquals(3, metrics.getComparisonCount(), "Comparison count should be 3.");
    }

    @Test
    void testAddComparisons() {
        // Add 7 comparisons
        metrics.addComparisons(7);

        // Check count
        assertEquals(7, metrics.getComparisonCount(), "Comparison count should be 7.");
    }

    @Test
    void testAddNegativeComparisonsThrows() {
        // Adding negative comparisons should throw error
        assertThrows(IllegalArgumentException.class, () -> metrics.addComparisons(-1));
    }

    @Test
    void testResetComparisonCount() {
        // Simulate comparisons
        metrics.addComparisons(10);

        // Reset count
        metrics.resetComparisonCount();

        // Check it is 0
        assertEquals(0, metrics.getComparisonCount(), "Comparison count should be reset to 0.");
    }

    @Test
    void testResetAll() {
        // Simulate full usage
        metrics.startTimer();
        metrics.addComparisons(5);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            fail("Sleep interrupted.");
        }
        metrics.stopTimer();

        // Reset everything
        metrics.resetAll();

        // Check all fields are zero
        assertEquals(0, metrics.getComparisonCount(), "Comparison count should be 0 after reset.");
        assertEquals(0, metrics.getElapsedTimeMs(), "Elapsed time should be 0 after reset.");
    }
}
