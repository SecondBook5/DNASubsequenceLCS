package jhu.edu.algos.lcs;

import jhu.edu.algos.utils.PerformanceMetrics;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for validating the brute-force LCS implementation.
 * Due to its exponential nature, this test is restricted to small inputs only.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LCSBruteForceTest {

    private LCSBruteForce algorithm;

    /**
     * Initializes a fresh instance of the brute-force LCS algorithm
     * before each test.
     */
    @BeforeEach
    void setup() {
        algorithm = new LCSBruteForce();
    }

    /**
     * Tests the brute-force algorithm on a small input pair
     * where the LCS is known and unique.
     */
    @Test
    void testKnownSmallLCS() {
        // Inputs: one common longest subsequence
        String s1 = "ACGT";
        String s2 = "AGT";
        String expected = "AGT";

        // Execute brute-force LCS
        LCSResult result = algorithm.computeLCS("Test1", s1, s2);

        // Assert LCS matches expectation
        assertEquals(expected, result.getLCS(), "Expected exact LCS match");
        assertEquals(expected.length(), result.getLCSLength(), "Length of LCS should match");
    }

    /**
     * Tests the algorithm when one string is empty.
     * Result should be an empty LCS.
     */
    @Test
    void testOneEmptyString() {
        LCSResult result = algorithm.computeLCS("Empty1", "ACGT", "");
        assertEquals("", result.getLCS(), "LCS should be empty when one input is empty");
    }

    /**
     * Tests both strings being empty.
     * Result should be an empty LCS.
     */
    @Test
    void testBothStringsEmpty() {
        LCSResult result = algorithm.computeLCS("EmptyBoth", "", "");
        assertEquals("", result.getLCS(), "LCS of two empty strings should be empty");
    }

    /**
     * Tests a case with no common characters.
     * Result should be an empty LCS.
     */
    @Test
    void testNoCommonCharacters() {
        LCSResult result = algorithm.computeLCS("NoCommon", "AAA", "GGG");
        assertEquals("", result.getLCS(), "No matching characters should produce empty LCS");
    }

    /**
     * Tests when the two strings are identical.
     * Result should be the full string itself.
     */
    @Test
    void testIdenticalStrings() {
        String s = "TAC";
        LCSResult result = algorithm.computeLCS("Identical", s, s);
        assertEquals(s, result.getLCS(), "Identical strings should return themselves as LCS");
    }

    /**
     * Tests defensive programming: null inputs should throw exceptions.
     */
    @Test
    void testNullInputsThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> algorithm.computeLCS("Null1", null, "ABC"));

        assertThrows(IllegalArgumentException.class,
                () -> algorithm.computeLCS("Null2", "ABC", null));
    }

    /**
     * Verifies that performance metrics (comparisons and time)
     * are recorded during LCS computation.
     */
    @Test
    void testMetricsAreTracked() {
        LCSResult result = algorithm.computeLCS("Metrics", "AG", "AG");

        // Metrics should be attached
        PerformanceMetrics metrics = result.getMetrics();

        // Should have at least 1 character comparison
        assertTrue(metrics.getComparisonCount() > 0, "Comparison count should be non-zero");

        // Elapsed time should be >= 0
        assertTrue(metrics.getElapsedTimeMs() >= 0, "Time should be non-negative");
    }
}
