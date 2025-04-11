package jhu.edu.algos.lcs;

import jhu.edu.algos.utils.PerformanceMetrics;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JUnit 5 test class for the LCSDynamic implementation.
 * Covers correctness, edge cases, and performance metrics across input scales.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LCSDynamicTest {

    private LCSDynamic algorithm;

    @BeforeEach
    void setup() {
        algorithm = new LCSDynamic();
    }

    @Test
    void testKnownCorrectness() {
        String s1 = "ACCGGTCGAGTGCGCGGAAGCCGGCCGAA";
        String s2 = "GTCGTTCGGAATGCCGTTGCTCTGTAAA";
        String expected = "GTCGTCGGAAGCCGGCCGAA";

        LCSResult result = algorithm.computeLCS("Known", s1, s2);
        PerformanceMetrics metrics = result.getMetrics();

        assertEquals(expected, result.getLCS(), "Mismatch in known correct LCS output.");
        assertEquals(expected.length(), result.getLCSLength());
        assertTrue(metrics.getComparisonCount() > 0);
        assertTrue(metrics.getElapsedTimeMs() >= 0);
        assertTrue(metrics.getEstimatedSpaceBytes() > 0, "Space should be > 0 for non-empty sequences");
        assertTrue(metrics.getEstimatedSpaceMB() > 0.0, "MB space should be > 0 for non-empty sequences");
    }

    @Test
    void testEmptyAndNullInputs() {
        // Case: both inputs empty
        assertThrows(IllegalArgumentException.class, () -> algorithm.computeLCS("Empty", "", ""));

        // Case: one empty
        assertThrows(IllegalArgumentException.class, () -> algorithm.computeLCS("OneEmpty1", "GATTACA", ""));

        assertThrows(IllegalArgumentException.class, () -> algorithm.computeLCS("OneEmpty2", "", "TAGC"));

        // Case: first null
        assertThrows(IllegalArgumentException.class, () -> algorithm.computeLCS("Null1", null, "TAGC"));

        // Case: second null
        assertThrows(IllegalArgumentException.class, () -> algorithm.computeLCS("Null2", "TAGC", null));

        // Case: both null
        assertThrows(IllegalArgumentException.class, () -> algorithm.computeLCS("BothNull", null, null));
    }


    @Test
    void testNoCommonCharacters() {
        LCSResult result = algorithm.computeLCS("NoCommon", "AAA", "GGG");
        assertEquals("", result.getLCS(), "No characters in common should return empty LCS.");
        assertTrue(result.getMetrics().getEstimatedSpaceBytes() > 0);
    }

    @Test
    void testIdenticalStrings() {
        String s = "TACGGTCA";
        LCSResult result = algorithm.computeLCS("Identical", s, s);
        assertEquals(s, result.getLCS(), "LCS of identical strings should be the string itself.");
        assertTrue(result.getMetrics().getEstimatedSpaceBytes() > 0);
    }

    @Test
    void testReversedStrings() {
        String s1 = "AGCT";
        String s2 = new StringBuilder(s1).reverse().toString();

        LCSResult result = algorithm.computeLCS("Reversed", s1, s2);

        assertTrue(result.getLCSLength() <= s1.length(), "LCS length must not exceed original string.");
        assertTrue(result.getMetrics().getEstimatedSpaceMB() > 0);
    }

    @Test
    void testOneCharacterOverlap() {
        LCSResult result = algorithm.computeLCS("OneChar", "A", "A");
        assertEquals("A", result.getLCS(), "LCS of 'A' vs 'A' should be 'A'.");
        assertEquals(1, result.getLCSLength());

        LCSResult result2 = algorithm.computeLCS("NoChar", "A", "B");
        assertEquals("", result2.getLCS(), "LCS of different single chars should be empty.");

        assertTrue(result.getMetrics().getEstimatedSpaceBytes() > 0);
        assertTrue(result2.getMetrics().getEstimatedSpaceBytes() > 0);
    }

    @Test
    void testScalingInputs() {
        List<String> sequences = generateGrowingInputs();

        for (int i = 0; i < sequences.size() - 1; i++) {
            String s1 = sequences.get(i);
            String s2 = sequences.get(i + 1);

            LCSResult result = algorithm.computeLCS("Scale_" + (i + 1), s1, s2);
            int m = s1.length();
            int n = s2.length();

            assertNotNull(result.getLCS(), "LCS should not be null.");
            assertTrue(result.getLCSLength() <= Math.min(m, n), "LCS should not exceed input bounds.");

            // Estimate upper bound (relaxed, not enforced)
            long estimated = (long) m * n;
            long actual = result.getMetrics().getComparisonCount();

            // Allow up to 2x m*n for full DP + traceback + defensive checks
            long softLimit = estimated * 2;

            if (actual > softLimit) {
                System.err.printf(" High comparison count: %d > %d for size (%d x %d)%n",
                        actual, softLimit, m, n);
            }

            assertTrue(result.getMetrics().getEstimatedSpaceBytes() >= (long) (m + 1) * (n + 1) * Integer.BYTES,
                    "Space should scale with DP matrix size");

            // Print result for plotting
            System.out.printf("LCS[%d x %d] | L: %d | Comparisons: %d | Time: %d ms | Space: %.3e MB%n",
                    m, n,
                    result.getLCSLength(),
                    actual,
                    result.getMetrics().getElapsedTimeMs(),
                    result.getMetrics().getEstimatedSpaceMB());
        }
    }

    /**
     * Generates progressively larger strings for performance scaling tests.
     *
     * @return a list of test strings of growing length.
     */
    private List<String> generateGrowingInputs() {
        List<String> inputs = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i * 10; j++) {
                sb.append("ACGT".charAt(j % 4));
            }
            inputs.add(sb.toString());
        }
        return inputs;
    }
}
