package jhu.edu.algos;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style test for LCSDriver.
 * Ensures required input file is processed and output is generated correctly.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LCSDriverTest {

    private static final String INPUT_FILE = "test_required_input.txt";
    private static final String OUTPUT_FILE = "test_required_input_output.txt";

    @BeforeAll
    void setup() throws IOException {
        // Write a minimal version of required input (S1–S4)
        List<String> lines = List.of(
                "S1 = ACCGGTCGACTGCGCGGAAGCCGGCCGAA",
                "S2 = GTCGTTCGGAATGCCGTTGCTCTGTAAA",
                "S3 = ATTGCATTGCATGGGCGCGATGCATTTGGTTAATTCCTCG",
                "S4 = CTTGCTTAAATGTGCA"
        );
        Files.write(Path.of(INPUT_FILE), lines);
    }

    @AfterAll
    void cleanup() throws IOException {
        Files.deleteIfExists(Path.of(INPUT_FILE));
        Files.deleteIfExists(Path.of(OUTPUT_FILE));
    }

    @Test
    void testDriverGeneratesOutputFile() throws Exception {
        // Call the driver logic directly
        LCSDriver.runFromFile(INPUT_FILE);

        // Check output file is created
        assertTrue(Files.exists(Path.of(OUTPUT_FILE)), "Output file should be created");

        // Check output contains expected content
        String outputContent = Files.readString(Path.of(OUTPUT_FILE));
        assertTrue(outputContent.contains("S1 = "), "Output should include input label");
        assertTrue(outputContent.contains("Pairwise Comparison: S1 vs S2"), "Output should include comparison");
        assertTrue(outputContent.contains("-- Dynamic Programming LCS --"), "Output should include Dynamic block");
        assertTrue(outputContent.contains("-- Brute Force LCS --"), "Output should include Brute Force block");
        assertTrue(outputContent.contains("Summary Table"), "Output should include summary section");
    }

    @Test
    void testFailsWithMissingFile() {
        Exception exception = assertThrows(IOException.class, () -> LCSDriver.runFromFile("nonexistent_file.txt"));
        assertTrue(exception.getMessage().contains("nonexistent_file"), "Should throw on missing file");
    }

    @Test
    void testFailsWithSingleSequence() throws Exception {
        String badFile = "test_one_seq.txt";
        Files.write(Path.of(badFile), List.of("S1 = ACTG"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> LCSDriver.runFromFile(badFile));

        assertTrue(exception.getMessage().contains("At least two sequences"));
        Files.deleteIfExists(Path.of(badFile));
    }
}
