package jhu.edu.algos.lcs;

import jhu.edu.algos.utils.PerformanceMetrics;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for validating the brute-force LCS implementation.
 * Covers both correctness and robustness, including edge cases, performance tracking,
 * and algorithm behavior under non-trivial scenarios.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LCSBruteForceTest {

    private LCSBruteForce algorithm;

    /**
     * Initializes a fresh instance of the brute-force LCS algorithm
     * before each test case to ensure no shared state.
     */
    @BeforeEach
    void setup() {
        algorithm = new LCSBruteForce();
    }

    /**
     * Verifies correctness on a known example where the LCS is unambiguous.
     * s1 = ACGT, s2 = AGT → LCS = AGT
     */
    @Test
    void testKnownSmallLCS() {
        String s1 = "ACGT";
        String s2 = "AGT";
        String expected = "AGT";

        LCSResult result = algorithm.computeLCS("Test1", s1, s2);

        assertEquals(expected, result.getLCS(), "Expected exact LCS match");
        assertEquals(expected.length(), result.getLCSLength(), "Length of LCS should match");
    }

    /**
     * Checks behavior when one of the strings is empty.
     * LCS should always be the empty string in such cases.
     */
    @Test
    void testOneEmptyString() {
        LCSResult result = algorithm.computeLCS("Empty1", "ACGT", "");
        assertEquals("", result.getLCS(), "LCS should be empty when one input is empty");
    }

    /**
     * Checks behavior when both input strings are empty.
     * The result should be an empty LCS.
     */
    @Test
    void testBothStringsEmpty() {
        LCSResult result = algorithm.computeLCS("EmptyBoth", "", "");
        assertEquals("", result.getLCS(), "LCS of two empty strings should be empty");
    }

    /**
     * Verifies that when no characters overlap between inputs,
     * the LCS is the empty string.
     */
    @Test
    void testNoCommonCharacters() {
        LCSResult result = algorithm.computeLCS("NoCommon", "AAA", "GGG");
        assertEquals("", result.getLCS(), "No matching characters should produce empty LCS");
    }

    /**
     * If both input strings are identical, the LCS should be the string itself.
     */
    @Test
    void testIdenticalStrings() {
        String s = "TAC";
        LCSResult result = algorithm.computeLCS("Identical", s, s);
        assertEquals(s, result.getLCS(), "Identical strings should return themselves as LCS");
    }

    /**
     * Ensures that the algorithm throws a clear error
     * when either input string is null.
     */
    @Test
    void testNullInputsThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> algorithm.computeLCS("Null1", null, "ABC"));
        assertThrows(IllegalArgumentException.class,
                () -> algorithm.computeLCS("Null2", "ABC", null));
    }

    /**
     * Verifies that performance metrics are recorded properly
     * and that both comparisons and timing are non-zero.
     */
    @Test
    void testMetricsAreTracked() {
        LCSResult result = algorithm.computeLCS("Metrics", "AG", "AG");
        PerformanceMetrics metrics = result.getMetrics();

        assertTrue(metrics.getComparisonCount() > 0, "Comparison count should be non-zero");
        assertTrue(metrics.getElapsedTimeMs() >= 0, "Time should be non-negative");
    }

    /**
     * Tests a moderately-sized input that still falls within tractable brute-force range.
     * This ensures performance remains stable for strings ~8 characters long.
     */
    @Test
    void testModerateLengthLCS() {
        String s1 = "ACGTACGT";
        String s2 = "TGCATCGT";

        LCSResult result = algorithm.computeLCS("Moderate", s1, s2);

        assertNotNull(result.getLCS(), "LCS should not be null");
        assertTrue(result.getLCSLength() > 0, "Should find some common subsequence");
        assertTrue(result.getMetrics().getComparisonCount() > 0, "Comparisons should be recorded");
    }

    /**
     * Ensures the LCS can be identified when it exists in the middle
     * of both input strings (not necessarily at the beginning or end).
     */
    @Test
    void testMiddleSubsequence() {
        String s1 = "GATC";
        String s2 = "CGATCGG";

        LCSResult result = algorithm.computeLCS("MiddleMatch", s1, s2);

        assertEquals("GATC", result.getLCS(), "Should find GATC as the full LCS");
    }

    /**
     * Tests a known ambiguous case with multiple valid LCS solutions.
     * Only checks for length and that one valid LCS is returned.
     */
    @Test
    void testMultipleLCSOptions() {
        String s1 = "ABCBDAB";
        String s2 = "BDCABA";

        LCSResult result = algorithm.computeLCS("MultiLCS", s1, s2);
        int len = result.getLCSLength();

        assertEquals(4, len, "Expected LCS length of 4");

        String lcs = result.getLCS();
        assertTrue(
                lcs.equals("BCAB") || lcs.equals("BDAB") || lcs.equals("BCBA") || lcs.equals("BDCA"),
                "LCS result should be one of the valid longest subsequences"
        );
    }
}
