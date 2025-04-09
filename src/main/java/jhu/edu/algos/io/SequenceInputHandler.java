package jhu.edu.algos.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SequenceInputHandler is responsible for reading DNA sequence data from a structured input file.
 * Each line of the input file is expected to follow the format:
 *     S1 = ACTG...
 *
 * This class parses the sequence identifier (e.g., "S1") and the corresponding nucleotide string,
 * returning a map of identifiers to sequences. It performs thorough error checking to ensure the file
 * is formatted correctly and all sequences are valid.
 */
public class SequenceInputHandler {

    /**
     * Reads a file containing labeled DNA sequences and returns them in a map.
     * The file must contain one sequence per line, in the format "Label = SEQUENCE".
     *
     * @param filePath The path to the input file containing the DNA sequences.
     * @return A Map where keys are sequence labels (e.g., "S1") and values are DNA sequences.
     * @throws IOException If an error occurs while reading the file or if the file format is invalid.
     */
    public static Map<String, String> readSequencesFromFile(String filePath) throws IOException {
        // Create a map to store the parsed sequences
        Map<String, String> sequenceMap = new HashMap<>();

        // Use BufferedReader to read the file line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            // Read each line of the file
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Trim leading/trailing whitespace
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                // Check that the line contains '=' as expected in the format
                if (!line.contains("=")) {
                    throw new IOException("Format error on line " + lineNumber + ": '=' delimiter missing.");
                }

                // Split the line into label and sequence parts
                String[] parts = line.split("=", 2);

                // Ensure exactly two parts after split
                if (parts.length != 2) {
                    throw new IOException("Format error on line " + lineNumber + ": improper label-sequence format.");
                }

                // Extract and clean up the label and sequence
                String label = parts[0].trim();
                String sequence = parts[1].trim();

                // Validate the label format
                if (!label.matches("S\\d+")) {
                    throw new IOException("Invalid label '" + label + "' on line " + lineNumber + ". Expected format: S1, S2, etc.");
                }

                // Ensure the sequence is not empty
                if (sequence.isEmpty()) {
                    throw new IOException("Empty sequence found for label '" + label + "' on line " + lineNumber + ".");
                }

                // Check for duplicates
                if (sequenceMap.containsKey(label)) {
                    throw new IOException("Duplicate label '" + label + "' found on line " + lineNumber + ".");
                }

                // Add the label and sequence to the map
                sequenceMap.put(label, sequence);
            }
        }

        // Return the final sequence map
        return sequenceMap;
    }
}
