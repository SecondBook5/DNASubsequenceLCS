package jhu.edu.algos.lcs;

import jhu.edu.algos.utils.PerformanceMetrics;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test class for the LCSResult data structure.
 * Verifies correctness of input storage, LCS values, metrics tracking,
 * and formatted output representation including the DP matrix.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LCSResultTest {

    // The test LCS result object to be initialized before each test
    private LCSResult result;

    // The performance metrics instance to attach to the result
    private PerformanceMetrics metrics;
    private int[][] dpMatrix;

    /**
     * Initializes a mock LCSResult object with known values
     * for use in all test cases.
     */
    @BeforeEach
    void setup() {
        // Mock input strings
        String s1 = "ACGT";
        String s2 = "AGT";

        // Expected LCS result from s1 and s2
        String lcs = "AGT";

        // Comparison label for testing metadata
        String label = "S1 vs S2";

        // Create and pre-populate metrics with mock values
        metrics = new PerformanceMetrics();
        metrics.addComparisons(42); // Simulate 42 comparisons
        metrics.setEstimatedSpaceBytes(20480); // 20 KB of space
        metrics.startTimer();
        metrics.stopTimer(); // Stop immediately for zero-time mock

        dpMatrix = new int[][] {
                {0, 0, 0, 0},
                {0, 1, 1, 1},
                {0, 1, 1, 1},
                {0, 1, 2, 2},
                {0, 1, 2, 3}
        };

        result = new LCSResult(label, s1, s2, lcs, metrics, dpMatrix);
    }

    /**
     * Tests that the label and input strings are stored correctly.
     */
    @Test
    void testLabelAndInputs() {
        assertEquals("S1 vs S2", result.getComparisonLabel());
        assertEquals("ACGT", result.getFirstInput());
        assertEquals("AGT", result.getSecondInput());
    }

    /**
     * Tests that the LCS string and its length are correctly returned.
     */
    @Test
    void testLCSStringAndLength() {
        assertEquals("AGT", result.getLCS());
        assertEquals(3, result.getLCSLength());
    }

    /**
     * Tests that the performance metrics are correctly attached and reported.
     */
    @Test
    void testPerformanceMetrics() {
        assertEquals(42, result.getMetrics().getComparisonCount());
        assertTrue(result.getMetrics().getElapsedTimeMs() >= 0);
        assertEquals(20480, result.getMetrics().getEstimatedSpaceBytes());
        assertTrue(result.getMetrics().getEstimatedSpaceMB() > 0);
    }

    @Test
    void testDPMatrixStorage() {
        int[][] matrix = result.getDpMatrix();
        assertNotNull(matrix, "DP matrix should not be null");

        assertEquals(5, matrix.length, "Matrix should have correct number of rows");
        assertEquals(4, matrix[0].length, "Matrix should have correct number of columns");

        assertEquals(3, matrix[4][3], "Matrix cell value mismatch at (4,3)");
    }

    @Test
    void testToStringOutputFormat() {
        // Capture the formatted result string
        String output = result.toString();
        assertTrue(output.contains("=== LCS Result: S1 vs S2 ==="));
        assertTrue(output.contains("Input 1: ACGT"));
        assertTrue(output.contains("Input 2: AGT"));
        assertTrue(output.contains("LCS    : AGT"));
        assertTrue(output.contains("Length : 3"));
        assertTrue(output.contains("Comparisons: 42"));
        assertTrue(output.contains("Time (ms)"));
        assertTrue(output.contains("Space (MB)"));

        // Validate scientific notation format
        assertTrue(output.matches("(?s).*Space \\(MB\\)\\s*:\\s*\\d\\.\\d{3}e[+-]?\\d+.*"),
                "Expected space output in scientific notation format");
    }
}
