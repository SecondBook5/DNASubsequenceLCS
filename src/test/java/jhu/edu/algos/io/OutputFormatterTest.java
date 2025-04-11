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

    @BeforeEach
    void setup() throws IOException {
        inputSequences = Map.of(
                "S1", "ACGT",
                "S2", "AGT",
                "S3", "CG"
        );

        dynResults = new ArrayList<>();
        bfResults = new ArrayList<>();

        dynResults.add(mockResult("S1 vs S2", "ACGT", "AGT", "AGT", 5, 1, 16384));
        dynResults.add(mockResult("S2 vs S3", "AGT", "CG", "G", 3, 1, 8192));

        bfResults.add(mockResult("S1 vs S2", "ACGT", "AGT", "AGT", 25, 10, 20480));
        bfResults.add(mockResult("S2 vs S3", "AGT", "CG", "G", 15, 8, 10240));

        OutputFormatter.writeResults(inputSequences, dynResults, bfResults, TEST_OUTPUT_FILE);
        outputContent = Files.readString(Path.of(TEST_OUTPUT_FILE));
    }

    @AfterEach
    void cleanupFile() {
        File file = new File(TEST_OUTPUT_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void testFileCreatedAndReadable() {
        assertTrue(Files.exists(Path.of(TEST_OUTPUT_FILE)), "Output file should exist");
        assertFalse(outputContent.isEmpty(), "Output file should not be empty");
    }

    @Test
    void testInputSequenceEchoed() {
        assertTrue(outputContent.contains("Number of sequences to be compared: 3"));

        // Assert that all input sequences are present with their correct lengths
        assertTrue(outputContent.contains("ACGT"), "Expected sequence ACGT to appear");
        assertTrue(outputContent.contains("AGT"), "Expected sequence AGT to appear");
        assertTrue(outputContent.contains("CG"), "Expected sequence CG to appear");

        assertTrue(outputContent.contains("Length: 4"), "Expected length of 4 (ACGT)");
        assertTrue(outputContent.contains("Length: 3"), "Expected length of 3 (AGT)");
        assertTrue(outputContent.contains("Length: 2"), "Expected length of 2 (CG)");
    }

    @Test
    void testPairwiseComparisonsPrinted() {
        assertTrue(outputContent.contains("Comparing sequences S1 vs S2"));
        assertTrue(outputContent.contains("Comparing sequences S2 vs S3"));
    }

    @Test
    void testLCSOutputPresent() {
        assertTrue(outputContent.contains("Longest common subsequence | Length: 3"));
        assertTrue(outputContent.contains("AGT"));
        assertTrue(outputContent.contains("G"));
    }

    @Test
    void testMatrixPrinted() {
        assertTrue(outputContent.contains("Printing out subsequence matrix"));
        assertTrue(outputContent.contains("A G T"));
        assertTrue(outputContent.contains("A "));
    }

    @Test
    void testAlgorithmHeadersPresent() {
        assertTrue(outputContent.contains("-- Dynamic Programming LCS --"));
        assertTrue(outputContent.contains("-- Brute Force LCS --"));
    }

    @Test
    void testPerformanceMetricsPrinted() {
        assertTrue(outputContent.contains("Comparisons: 25"));
        assertTrue(outputContent.contains("Time (ms)"));
        assertTrue(outputContent.contains("Space (MB)"));
    }

    @Test
    void testScientificNotationSpaceMetric() {
        assertTrue(outputContent.matches("(?s).*Space \\(MB\\).*1\\.\\d+e[+-]?\\d+.*"));
    }

    @Test
    void testSummaryTablePresentAndCorrect() {
        assertTrue(outputContent.contains("Summary Table (Comparisons, Time, and Space)"));
        assertTrue(outputContent.contains("S1 vs S2"));
        assertTrue(outputContent.contains("S2 vs S3"));
        assertTrue(outputContent.contains("| 3"));
        assertTrue(outputContent.contains("| 1"));
    }

    /**
     * Helper method to mock an LCSResult with fake metrics.
     */
    private LCSResult mockResult(String label, String s1, String s2, String lcs,
                                 long comparisons, long timeMs, long spaceBytes) {
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.addComparisons(comparisons);
        metrics.setEstimatedSpaceBytes(spaceBytes);
        metrics.startTimer();
        try {
            Thread.sleep(timeMs);
        } catch (InterruptedException ignored) {
        }
        metrics.stopTimer();
        return new LCSResult(label, s1, s2, lcs, metrics);
    }
}
