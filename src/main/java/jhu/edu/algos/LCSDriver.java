package jhu.edu.algos;

import jhu.edu.algos.io.OutputFormatter;
import jhu.edu.algos.io.SequenceInputHandler;
import jhu.edu.algos.lcs.*;

import java.util.*;

/**
 * Driver for running LCS comparisons on the required input (S1 to S4).
 * Executes both Dynamic Programming and Brute Force implementations.
 * Results are written to console and output file using OutputFormatter.
 */
public class LCSDriver {

    /**
     * CLI entry point for running pairwise LCS comparisons.
     * Accepts two arguments: input file and output file.
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java LCSDriver <input_file.txt> <output_file.txt>");
            System.exit(1);
        }

        try {
            runFromFile(args[0], args[1]);
        } catch (IllegalArgumentException e) {
            System.err.println("Input Error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Runs the LCS comparison on all pairwise combinations of sequences from the input file.
     * Results are written to both the console and the specified output file.
     *
     * @param inputFile  Path to the input file (must contain at least two sequences).
     * @param outputFile Path to the output file (.txt) where results are saved.
     * @throws Exception if the input is invalid or comparison fails.
     */
    public static void runFromFile(String inputFile, String outputFile) throws Exception {
        // Step 1: Load and validate input sequences
        Map<String, String> inputSequences = SequenceInputHandler.readSequencesFromFile(inputFile);

        if (inputSequences.size() < 2) {
            throw new IllegalArgumentException("At least two sequences are required for comparison.");
        }

        // Step 2: Prepare LCS algorithms and pairwise result storage
        List<String> keys = new ArrayList<>(inputSequences.keySet());
        List<LCSResult> dynamicResults = new ArrayList<>();
        List<LCSResult> bruteForceResults = new ArrayList<>();

        AbstractLCS dynAlg = new LCSDynamic();
        AbstractLCS bruteAlg = new LCSBruteForce();

        // Step 3: Compute all pairwise LCS results
        for (int i = 0; i < keys.size(); i++) {
            for (int j = i + 1; j < keys.size(); j++) {
                String k1 = keys.get(i);
                String k2 = keys.get(j);

                String s1 = inputSequences.get(k1);
                String s2 = inputSequences.get(k2);
                String label = k1 + " vs " + k2;

                LCSResult dynResult = dynAlg.computeLCS(label, s1, s2);
                LCSResult bruteResult = bruteAlg.computeLCS(label, s1, s2);

                dynamicResults.add(dynResult);
                bruteForceResults.add(bruteResult);
            }
        }

        // Step 4: Output results to file and console
        OutputFormatter.writeResults(inputSequences, dynamicResults, bruteForceResults, outputFile);
    }
}
