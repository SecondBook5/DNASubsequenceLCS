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

    @AfterEach
    void cleanupFile() {
        File file = new File(TEST_OUTPUT_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Full test that checks input echo, both LCS algorithm outputs, and summary formatting.
     */
    @Test
    void testOutputFormatterWritesAllExpectedContent() throws IOException {
        // === Prepare input sequences ===
        Map<String, String> inputSequences = Map.of(
                "S1", "ACGT",
                "S2", "AGT",
                "S3", "CG"
        );

        // === Prepare mock results for S1 vs S2 and S2 vs S3 ===
        List<LCSResult> dynResults = new ArrayList<>();
        List<LCSResult> bfResults = new ArrayList<>();

        dynResults.add(mockResult("S1 vs S2", "ACGT", "AGT", "AGT", 5, 1));
        dynResults.add(mockResult("S2 vs S3", "AGT", "CG", "G", 3, 1));

        bfResults.add(mockResult("S1 vs S2", "ACGT", "AGT", "AGT", 25, 10));
        bfResults.add(mockResult("S2 vs S3", "AGT", "CG", "G", 15, 8));

        // === Write the output ===
        OutputFormatter.writeResults(inputSequences, dynResults, bfResults, TEST_OUTPUT_FILE);

        // === Confirm file created and readable ===
        Path path = Path.of(TEST_OUTPUT_FILE);
        assertTrue(Files.exists(path), "Expected output file to be created.");

        // === Read contents ===
        String content = Files.readString(path);

        // === Validate input echo ===
        assertTrue(content.contains("S1 = ACGT"), "Should echo S1 input");
        assertTrue(content.contains("S2 = AGT"), "Should echo S2 input");
        assertTrue(content.contains("S3 = CG"), "Should echo S3 input");

        // === Validate pairwise block headers ===
        assertTrue(content.contains("Pairwise Comparison: S1 vs S2"), "Missing block for S1 vs S2");
        assertTrue(content.contains("Pairwise Comparison: S2 vs S3"), "Missing block for S2 vs S3");

        // === Validate both algorithm blocks ===
        assertTrue(content.contains("-- Dynamic Programming LCS --"), "Missing dynamic section");
        assertTrue(content.contains("-- Brute Force LCS --"), "Missing brute force section");

        // === Validate LCS strings and lengths ===
        assertTrue(content.contains("LCS        : AGT"), "Missing LCS AGT");
        assertTrue(content.contains("Length     : 3"), "Missing LCS length for AGT");
        assertTrue(content.contains("LCS        : G"), "Missing LCS G");

        // === Validate comparisons and time ===
        assertTrue(content.contains("Comparisons: 25"), "Missing brute-force comparisons");
        assertTrue(content.contains("Time (ms)"), "Missing brute-force time label");


        // === Validate summary table section ===
        assertTrue(content.contains("Summary Table"), "Missing summary header");
        assertTrue(content.contains("S1 vs S2"), "Summary should list S1 vs S2");
        assertTrue(content.contains("S2 vs S3"), "Summary should list S2 vs S3");

        // === Optional: Check that AGT and G appear in summary as lengths ===
        assertTrue(content.contains("| 3"), "Summary table should include length 3");
        assertTrue(content.contains("| 1"), "Summary table should include length 1");
    }

    /**
     * Generates a mock LCSResult with preset metrics and values.
     */
    private LCSResult mockResult(String label, String s1, String s2, String lcs,
                                 long comparisons, long timeMs) {
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.addComparisons(comparisons);
        metrics.startTimer();
        try { Thread.sleep(1); } catch (InterruptedException ignored) {} // simulate delay
        metrics.stopTimer(); // simulated time
        return new LCSResult(label, s1, s2, lcs, metrics);
    }
}
