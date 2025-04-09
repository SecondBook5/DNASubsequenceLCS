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

        assertEquals(expected, result.getLCS(), "Mismatch in known correct LCS output.");
        assertEquals(expected.length(), result.getLCSLength());
        assertTrue(result.getMetrics().getComparisonCount() > 0);
        assertTrue(result.getMetrics().getElapsedTimeMs() >= 0);
    }

    @Test
    void testEmptyAndNullInputs() {
        // Empty both
        LCSResult bothEmpty = algorithm.computeLCS("Empty", "", "");
        assertEquals("", bothEmpty.getLCS());
        assertEquals(0, bothEmpty.getLCSLength());

        // One empty
        LCSResult oneEmpty = algorithm.computeLCS("OneEmpty", "GATTACA", "");
        assertEquals("", oneEmpty.getLCS());

        // Nulls
        assertThrows(IllegalArgumentException.class,
                () -> algorithm.computeLCS("Null1", null, "TAGC"));

        assertThrows(IllegalArgumentException.class,
                () -> algorithm.computeLCS("Null2", "TAGC", null));
    }

    @Test
    void testNoCommonCharacters() {
        LCSResult result = algorithm.computeLCS("NoCommon", "AAA", "GGG");
        assertEquals("", result.getLCS());
    }

    @Test
    void testIdenticalStrings() {
        String s = "TACGGTCA";
        LCSResult result = algorithm.computeLCS("Identical", s, s);
        assertEquals(s, result.getLCS());
    }

    @Test
    void testReversedStrings() {
        String s1 = "AGCT";
        String s2 = new StringBuilder(s1).reverse().toString();

        LCSResult result = algorithm.computeLCS("Reversed", s1, s2);

        // Should be a single common character (unless palindromic)
        assertTrue(result.getLCSLength() <= 2);
    }

    @Test
    void testOneCharacterOverlap() {
        LCSResult result = algorithm.computeLCS("OneChar", "A", "A");
        assertEquals("A", result.getLCS());
        assertEquals(1, result.getLCSLength());

        LCSResult result2 = algorithm.computeLCS("NoChar", "A", "B");
        assertEquals("", result2.getLCS());
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

            // Ensure LCS is not null and not longer than either string
            assertNotNull(result.getLCS(), "LCS result should not be null");
            assertTrue(result.getLCSLength() <= Math.min(m, n), "LCS length exceeds bounds");

            // Estimate upper bound (relaxed, not enforced)
            long estimated = (long) m * n;
            long actual = result.getMetrics().getComparisonCount();

            // Allow up to 2x m*n for full DP + traceback + defensive checks
            long softLimit = estimated * 2;

            if (actual > softLimit) {
                System.err.printf(" High comparison count: %d > %d for size (%d x %d)%n",
                        actual, softLimit, m, n);
            }

            // Ensure timing is non-negative
            assertTrue(result.getMetrics().getElapsedTimeMs() >= 0, "Elapsed time should be non-negative");

            // Print stats for graphing or debugging
            System.out.printf("LCS[%d x %d] | L: %d | Comp: %d | Time: %d ms%n",
                    m, n,
                    result.getLCSLength(),
                    actual,
                    result.getMetrics().getElapsedTimeMs());
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
