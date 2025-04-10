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
     * Full test that checks input echo, both LCS algorithm outputs, matrix, and summary formatting.
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
        assertTrue(content.contains("Number of sequences to be compared: 3"), "Should list input size");
        assertTrue(content.contains("Sequence #1 | Length: " + inputSequences.get("S1").length()), "Should list correct S1 size");
        assertTrue(content.contains("ACGT"), "Should echo sequence S1");
        assertTrue(content.contains("AGT"), "Should echo sequence S2");
        assertTrue(content.contains("CG"), "Should echo sequence S3");

        // === Validate pairwise comparison section ===
        assertTrue(content.contains("Comparing sequences S1 vs S2"), "Missing comparison block for S1 vs S2");
        assertTrue(content.contains("Comparing sequences S2 vs S3"), "Missing comparison block for S2 vs S3");

        // === Validate LCS summary ===
        assertTrue(content.contains("Longest common subsequence | Length: 3"), "Missing LCS length for AGT");
        assertTrue(content.contains("AGT"), "Missing LCS value AGT");
        assertTrue(content.contains("G"), "Missing LCS value G");

        // === Validate matrix block ===
        assertTrue(content.contains("Printing out subsequence matrix"), "Should include DP matrix section");
        assertTrue(content.contains("A G T"), "Should include s2 header row");
        assertTrue(content.contains("A "), "Should include s1 row");

        // === Validate algorithm headers ===
        assertTrue(content.contains("-- Dynamic Programming LCS --"), "Missing dynamic algorithm header");
        assertTrue(content.contains("-- Brute Force LCS --"), "Missing brute force algorithm header");

        // === Validate metrics section ===
        assertTrue(content.contains("Comparisons: 25"), "Should show brute-force comparisons");
        assertTrue(content.contains("Time (ms)"), "Should show brute-force time label");


        // === Validate summary table ===
        assertTrue(content.contains("Summary Table (Comparisons and Time)"), "Missing summary table header");
        assertTrue(content.contains("S1 vs S2"), "Missing summary entry for S1 vs S2");
        assertTrue(content.contains("S2 vs S3"), "Missing summary entry for S2 vs S3");
        assertTrue(content.contains("| 3"), "Should include LCS length 3");
        assertTrue(content.contains("| 1"), "Should include LCS length 1");
    }

    /**
     * Helper method to mock an LCSResult with fake metrics.
     */
    private LCSResult mockResult(String label, String s1, String s2, String lcs,
                                 long comparisons, long timeMs) {
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.addComparisons(comparisons);
        metrics.startTimer();
        metrics.stopTimer();
        return new LCSResult(label, s1, s2, lcs, metrics);
    }
}
