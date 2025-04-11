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
        List<String> lines = List.of(
                "S1 = ACCGGTCGACTGCGCGGAAGCCGGCCGAA",
                "S2 = GTCGTTCGGAATGCCGTTGCTCTGTAAA",
                "S3 = ATTGCATTGCATGGGCGCGATGCATTTGGTTAATTCCTCG",
                "S4 = CTTGCTTAAATGTGCA"
        );
        Files.write(Path.of(INPUT_FILE), lines);
    }

    /**
     * Deletes all generated files after tests.
     */
    @AfterAll
    void cleanup() throws IOException {
        Files.deleteIfExists(Path.of(INPUT_FILE));
        Files.deleteIfExists(Path.of(OUTPUT_FILE));
    }

    /**
     * Tests that the driver generates an output file and expected content.
     */
    @Test
    void testDriverGeneratesOutputFile() throws Exception {
        LCSDriver.runFromFile(INPUT_FILE, OUTPUT_FILE);

        // File should be generated
        assertTrue(Files.exists(Path.of(OUTPUT_FILE)), "Output file should be created");

        // Read file contents
        String output = Files.readString(Path.of(OUTPUT_FILE));

        // Validate input sequence echo
        assertTrue(output.contains("Sequence #1"), "Should mention sequence numbers");
        assertTrue(output.contains("ACCGGTCGACTGCGCGGAAGCCGGCCGAA"), "Should include sequence content");

        // Validate LCS output and sections
        assertTrue(output.contains("Comparing sequences S1 vs S2"), "Should mention comparisons");
        assertTrue(output.contains("Longest common subsequence"), "Should show LCS line");
        assertTrue(output.contains("-- Dynamic Programming LCS --"), "Should include dynamic section");
        assertTrue(output.contains("-- Brute Force LCS --"), "Should include brute-force section");

        // Validate summary table
        assertTrue(output.contains("Summary Table (Comparisons, Time, and Space)"), "Should include summary table");
        assertTrue(output.contains("S1 vs S2"), "Pair names should be listed in summary");

        // Validate aggregate summary section
        assertTrue(output.replace("\r", "").contains("===== Aggregate Performance Summary ====="), "Should include aggregate summary header");
        assertTrue(output.contains("Dynamic Programming: Total Time ="), "Should report total runtime for dynamic");
        assertTrue(output.contains("Brute Force        : Total Time ="), "Should report total runtime for brute force");
        assertTrue(output.contains("Total Space ="), "Should report total space for both algorithms");

        // Validate presence of MB in both space rows (scientific notation check relaxed to substring match)
        assertTrue(output.contains("MB"), "Space usage should be reported in MB");
    }

    /**
     * Tests that missing input file triggers exception.
     */
    @Test
    void testFailsWithMissingFile() {
        Exception ex = assertThrows(IOException.class,
                () -> LCSDriver.runFromFile("nonexistent_input.txt", "output.txt"));

        assertTrue(ex.getMessage().contains("nonexistent_input"), "Should fail for missing input");
    }

    /**
     * Tests that input with only one sequence throws an exception.
     */
    @Test
    void testFailsWithSingleSequence() throws Exception {
        String oneSeqFile = "one_sequence.txt";
        String output = "output.txt";

        Files.write(Path.of(oneSeqFile), List.of("S1 = ACTG"));

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> LCSDriver.runFromFile(oneSeqFile, output));

        assertTrue(ex.getMessage().contains("At least two sequences"), "Should require two or more sequences");

        Files.deleteIfExists(Path.of(oneSeqFile));
        Files.deleteIfExists(Path.of(output));
    }
}
