package jhu.edu.algos.io;

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for SequenceInputHandler.
 * Creates and deletes test files dynamically for validation and error handling.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SequenceInputHandlerTest {

    private static final String TEMP_DIR = "temp_test_inputs";

    @BeforeAll
    void setupDirectory() {
        new File(TEMP_DIR).mkdirs();
    }

    @AfterAll
    void cleanupDirectory() {
        File dir = new File(TEMP_DIR);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
        dir.delete();
    }

    @Test
    void testValidInput() throws Exception {
        String path = TEMP_DIR + "/valid.txt";
        writeToFile(path,
                """
                        S1 = ACCGGTCGACTGCGCGGAAGCCGGCCGAA
                        S2 = GTCGTTCGGAATGCCGTTGCTCTGTAAA
                        S3 = ATTGCATTGCATGGGCGCGATGCATTTGGTTAATTCCTCG
                        S4 = CTTGCTTAAATGTGCA"""
        );

        Map<String, String> result = SequenceInputHandler.readSequencesFromFile(path);

        // Verify total number of entries
        assertEquals(4, result.size(), "Expected 4 sequences");

        // Expected sequences
        Map<String, String> expected = Map.of(
                "S1", "ACCGGTCGACTGCGCGGAAGCCGGCCGAA",
                "S2", "GTCGTTCGGAATGCCGTTGCTCTGTAAA",
                "S3", "ATTGCATTGCATGGGCGCGATGCATTTGGTTAATTCCTCG",
                "S4", "CTTGCTTAAATGTGCA"
        );

        // Check that keys exist and values match exactly
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String label = entry.getKey();
            String expectedSeq = entry.getValue();

            assertTrue(result.containsKey(label), "Missing label: " + label);
            assertEquals(expectedSeq, result.get(label), "Incorrect sequence for label: " + label);
        }

        System.out.println(" Retrieval test passed for all expected sequences.");
    }

    @Test
    void testInsertionOrderPreserved() throws Exception {
        String path = TEMP_DIR + "/ordered.txt";
        List<String> expectedLabels = List.of("S3", "S1", "S2");
        writeToFile(path,
                """
                        S3 = GATTACA
                        S1 = ACTG
                        S2 = CGTA"""
        );

        Map<String, String> result = SequenceInputHandler.readSequencesFromFile(path);
        List<String> actualLabels = new ArrayList<>(result.keySet());

        assertEquals(expectedLabels, actualLabels, "Insertion order of sequence labels should be preserved.");
    }

    @Test
    void testMissingFile() {
        String path = TEMP_DIR + "/nonexistent.txt";
        assertThrows(IOException.class, () -> SequenceInputHandler.readSequencesFromFile(path), "Expected IOException for missing file");
    }

    @Test
    void testMalformedLine() {
        String path = TEMP_DIR + "/malformed.txt";
        writeToFile(path,
                "S1 ACCGGTCGACTGCGCGGAAGCCGGCCGAA\n" +  // Missing '='
                        "S2 = GTCGTTCGGAATGCCGTTGCTCTGTAAA"
        );

        assertThrows(IOException.class, () -> SequenceInputHandler.readSequencesFromFile(path), "Expected IOException for malformed input");
    }

    @Test
    void testDuplicateLabel() {
        String path = TEMP_DIR + "/duplicate.txt";
        writeToFile(path,
                """
                        S1 = ACTG
                        S2 = TGCA
                        S2 = GATTACA"""
        );

        assertThrows(IOException.class, () -> SequenceInputHandler.readSequencesFromFile(path), "Expected IOException for duplicate label");
    }

    @Test
    void testEmptySequence() {
        String path = TEMP_DIR + "/empty.txt";
        writeToFile(path,
                "S1 = ACTG\n" +
                        "S2 = "
        );

        assertThrows(IOException.class, () -> SequenceInputHandler.readSequencesFromFile(path), "Expected IOException for empty sequence");
    }

    @Test
    void testInvalidLabel() {
        String path = TEMP_DIR + "/invalidlabel.txt";
        writeToFile(path,
                "SEQ1 = ACTGACTG\n" +  // Invalid label format
                        "S2 = GTCGTT"
        );

        assertThrows(IOException.class, () -> SequenceInputHandler.readSequencesFromFile(path), "Expected IOException for invalid label format");
    }

    private void writeToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            fail("Failed to write test file: " + filePath);
        }
    }
}
