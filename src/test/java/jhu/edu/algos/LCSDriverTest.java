package jhu.edu.algos;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style test for LCSDriver.
 * Ensures required input file is processed and output is generated correctly.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LCSDriverTest {

    private static final String INPUT_FILE = "test_required_input.txt";
    private static final String OUTPUT_FILE = "test_required_output.txt";

    /**
     * Creates a temporary valid input file before tests begin.
     */
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

    /**
     * Removes all generated files after test completion.
     */
    @AfterAll
    void cleanup() throws IOException {
        Files.deleteIfExists(Path.of(INPUT_FILE));
        Files.deleteIfExists(Path.of(OUTPUT_FILE));
    }

    /**
     * Tests that the driver generates an output file and includes key content.
     */
    @Test
    void testDriverGeneratesOutputFile() throws Exception {
        // Call updated driver method with both input and output
        LCSDriver.runFromFile(INPUT_FILE, OUTPUT_FILE);

        // Check output file is created
        assertTrue(Files.exists(Path.of(OUTPUT_FILE)), "Output file should be created");

        // Check output contains expected content
        String outputContent = Files.readString(Path.of(OUTPUT_FILE));
        assertTrue(outputContent.contains("S1 = "), "Output should echo input labels");
        assertTrue(outputContent.contains("Pairwise Comparison: S1 vs S2"), "Should contain labeled comparison");
        assertTrue(outputContent.contains("-- Dynamic Programming LCS --"), "Should contain dynamic block");
        assertTrue(outputContent.contains("-- Brute Force LCS --"), "Should contain brute-force block");
        assertTrue(outputContent.contains("Summary Table"), "Should include summary section");
    }

    /**
     * Tests that missing input file throws IOException.
     */
    @Test
    void testFailsWithMissingFile() {
        Exception exception = assertThrows(IOException.class,
                () -> LCSDriver.runFromFile("nonexistent_input.txt", "unused_output.txt"));

        assertTrue(exception.getMessage().contains("nonexistent_input"), "Should throw on missing input file");
    }

    /**
     * Tests that input with only one sequence throws IllegalArgumentException.
     */
    @Test
    void testFailsWithSingleSequence() throws Exception {
        String badInput = "test_one_sequence.txt";
        Files.write(Path.of(badInput), List.of("S1 = ACTG"));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> LCSDriver.runFromFile(badInput, "test_output.txt"));

        assertTrue(exception.getMessage().contains("At least two sequences"), "Should require minimum two sequences");
        Files.deleteIfExists(Path.of(badInput));
        Files.deleteIfExists(Path.of("test_output.txt"));
    }
}
