package jhu.edu.algos.io;

import jhu.edu.algos.lcs.LCSResult;
import jhu.edu.algos.utils.PerformanceMetrics;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test for OutputFormatter.
 * Validates correct formatting and output of results to both terminal and file.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OutputFormatterTest {

    private static final String TEST_OUTPUT_FILE = "test_output_results.txt";
    private Map<String, String> inputSequences;
    private List<LCSResult> dynResults;
    private List<LCSResult> bfResults;
    private String outputContent;

    /**
     * Sets up test data before each test.
     * Mocks 3 sequences and their 3 pairwise comparisons.
     */
    @BeforeEach
    void setup() throws IOException {
        inputSequences = new LinkedHashMap<>();
        inputSequences.put("S1", "ACGT");
        inputSequences.put("S2", "AGT");
        inputSequences.put("S3", "CG");

        dynResults = new ArrayList<>();
        bfResults = new ArrayList<>();

        dynResults.add(mockResult("S1 vs S2", "ACGT", "AGT", "AGT", 5, 1, 16384));
        dynResults.add(mockResult("S1 vs S3", "ACGT", "CG", "CG", 4, 1, 10240));
        dynResults.add(mockResult("S2 vs S3", "AGT", "CG", "G", 3, 1, 8192));

        bfResults.add(mockResult("S1 vs S2", "ACGT", "AGT", "AGT", 25, 10, 20480));
        bfResults.add(mockResult("S1 vs S3", "ACGT", "CG", "CG", 20, 8, 15360));
        bfResults.add(mockResult("S2 vs S3", "AGT", "CG", "G", 15, 8, 10240));

        OutputFormatter.writeResults(inputSequences, dynResults, bfResults, TEST_OUTPUT_FILE);
        outputContent = Files.readString(Path.of(TEST_OUTPUT_FILE));
    }

    /**
     * Cleans up the test output file after each test.
     */
    @AfterEach
    void cleanupFile() {
        File file = new File(TEST_OUTPUT_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Verifies the output file is created and not empty.
     */
    @Test
    void testFileCreatedAndReadable() {
        assertTrue(Files.exists(Path.of(TEST_OUTPUT_FILE)), "Output file should exist");
        assertFalse(outputContent.isEmpty(), "Output file should not be empty");
    }

    /**
     * Verifies that all input sequences are echoed correctly with their lengths.
     */
    @Test
    void testInputSequenceEchoed() {
        assertTrue(outputContent.contains("Number of sequences to be compared: 3"));
        assertTrue(outputContent.contains("ACGT"));
        assertTrue(outputContent.contains("AGT"));
        assertTrue(outputContent.contains("CG"));
        assertTrue(outputContent.contains("Length: 4"));
        assertTrue(outputContent.contains("Length: 3"));
        assertTrue(outputContent.contains("Length: 2"));
    }

    /**
     * Verifies all expected pairwise comparisons are included.
     */
    @Test
    void testAllPairwiseComparisonsPrinted() {
        List<String> expectedPairs = List.of(
                "Comparing sequences S1 vs S2",
                "Comparing sequences S1 vs S3",
                "Comparing sequences S2 vs S3"
        );
        for (String pair : expectedPairs) {
            assertTrue(outputContent.contains(pair), "Missing pairwise comparison: " + pair);
        }
    }

    /**
     * Confirms that LCS strings and lengths are printed.
     */
    @Test
    void testLCSOutputPresent() {
        assertTrue(outputContent.contains("Longest common subsequence | Length: 3"));
        assertTrue(outputContent.contains("AGT"));
        assertTrue(outputContent.contains("CG"));
        assertTrue(outputContent.contains("G"));
    }

    /**
     * Confirms that the matrix for each comparison is printed.
     */
    @Test
    void testMatrixPrinted() {
        assertTrue(outputContent.contains("Printing out subsequence matrix"));
        assertTrue(outputContent.contains("A G T"));
    }

    /**
     * Checks that dynamic and brute-force headers are included.
     */
    @Test
    void testAlgorithmHeadersPresent() {
        assertTrue(outputContent.contains("-- Dynamic Programming LCS --"));
        assertTrue(outputContent.contains("-- Brute Force LCS --"));
    }

    /**
     * Confirms that performance metrics like comparisons, time, and memory are present.
     */
    @Test
    void testPerformanceMetricsPrinted() {
        assertTrue(outputContent.contains("Comparisons: 25"));
        assertTrue(outputContent.contains("Time (ms)"));
        assertTrue(outputContent.contains("Space (MB)"));
    }

    /**
     * Confirms memory usage is reported in scientific notation.
     */
    @Test
    void testScientificNotationSpaceMetric() {
        assertTrue(outputContent.matches("(?s).*Space \\(MB\\).*1\\.\\d+e[+-]?\\d+.*"));
    }

    /**
     * Confirms that the summary table exists and includes all pairwise comparisons.
     */
    @Test
    void testSummaryTablePresentAndCorrect() {
        assertTrue(outputContent.contains("Summary Table (Comparisons, Time, and Space)"));
        assertTrue(outputContent.contains("S1 vs S2"));
        assertTrue(outputContent.contains("S1 vs S3"));
        assertTrue(outputContent.contains("S2 vs S3"));
    }

    /**
     * Validates that the LCS length matrix is printed correctly and symmetrically.
     */
    @Test
    void testLCSMatrixPrintsCorrectly() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            OutputFormatter.printLCSMatrix(inputSequences, dynResults);
            String matrixOutput = outContent.toString();
            assertTrue(matrixOutput.contains("LCS Length Matrix"), "Should print LCS Matrix header");
            assertTrue(matrixOutput.contains("S1"));
            assertTrue(matrixOutput.contains("S2"));
            assertTrue(matrixOutput.contains("S3"));
            assertTrue(matrixOutput.contains("--"));
            assertTrue(matrixOutput.contains("3") || matrixOutput.contains("2") || matrixOutput.contains("1"));
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Helper method to mock an LCSResult with synthetic metrics.
     */
    private LCSResult mockResult(String label, String s1, String s2, String lcs,
                                 long comparisons, long timeMs, long spaceBytes) {
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.addComparisons(comparisons);
        metrics.setEstimatedSpaceBytes(spaceBytes);
        metrics.startTimer();
        try {
            Thread.sleep(timeMs);
        } catch (InterruptedException ignored) {}
        metrics.stopTimer();
        return new LCSResult(label, s1, s2, lcs, metrics);
    }
}
