package jhu.edu.algos;

import jhu.edu.algos.io.OutputFormatter;
import jhu.edu.algos.io.SequenceInputHandler;
import jhu.edu.algos.lcs.*;
import jhu.edu.algos.utils.PerformanceMetrics;

import java.util.*;

/**
 * Driver for running LCS comparisons on the required input (S1 to S4 or more).
 * Executes both Dynamic Programming and Brute Force implementations.
 * Results are written to console and output file using OutputFormatter.
 */
public class LCSDriver {

    // Maximum sequence length allowed for brute force (to avoid blow-up)
    private static final int BRUTE_FORCE_CAP = 25;

    /**
     * CLI entry point for running pairwise LCS comparisons.
     * Accepts arguments:
     *   <input.txt> <output.txt> [--matrix]
     */
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: java LCSDriver <input_file.txt> <output_file.txt> [--matrix]");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];
        boolean showMatrix = args.length == 3 && args[2].equals("--matrix");

        try {
            runFromFile(inputFile, outputFile, showMatrix);
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
     * @param inputFile   Path to the input file (must contain at least two sequences).
     * @param outputFile  Path to the output file (.txt) where results are saved.
     * @param showMatrix  Whether to print the LCS length matrix to console.
     * @throws Exception  if the input is invalid or comparison fails.
     */
    public static void runFromFile(String inputFile, String outputFile, boolean showMatrix) throws Exception {
        if (!outputFile.endsWith(".txt")) {
            throw new IllegalArgumentException("Output file must have a .txt extension.");
        }

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

        long totalDynTime = 0;
        long totalDynSpace = 0;
        long totalBFTime = 0;
        long totalBFSpace = 0;

        // Step 3: Compute all pairwise LCS results
        for (int i = 0; i < keys.size(); i++) {
            for (int j = i + 1; j < keys.size(); j++) {
                String k1 = keys.get(i);
                String k2 = keys.get(j);

                String s1 = inputSequences.get(k1);
                String s2 = inputSequences.get(k2);
                String label = k1 + " vs " + k2;

                LCSResult dynResult = dynAlg.computeLCS(label, s1, s2);
                dynamicResults.add(dynResult);
                totalDynTime += dynResult.getMetrics().getElapsedTimeMs();
                totalDynSpace += dynResult.getMetrics().getEstimatedSpaceBytes();

                // Run brute force only if both strings are short enough
                LCSResult bruteResult;
                if (s1.length() <= BRUTE_FORCE_CAP && s2.length() <= BRUTE_FORCE_CAP) {
                    bruteResult = bruteAlg.computeLCS(label, s1, s2);
                    totalBFTime += bruteResult.getMetrics().getElapsedTimeMs();
                    totalBFSpace += bruteResult.getMetrics().getEstimatedSpaceBytes();
                } else {
                    // Skip and substitute with empty result
                    bruteResult = new LCSResult(label, s1, s2, "-", new PerformanceMetrics());
                }
                bruteForceResults.add(bruteResult);
            }
        }

        // Step 4: Output results to file and console
        OutputFormatter.writeResults(inputSequences, dynamicResults, bruteForceResults, outputFile);

        // Step 5: Append performance summary to the output file
        OutputFormatter.appendPerformanceSummary(
                totalDynTime, totalDynSpace, totalBFTime, totalBFSpace, outputFile
        );

        // Step 6: Also print to console
        System.out.println("\n===== Aggregate Performance Summary =====");
        System.out.printf("Dynamic Programming: Total Time = %d ms | Total Space = %.3f MB%n",
                totalDynTime, totalDynSpace / 1_000_000.0);
        System.out.printf("Brute Force        : Total Time = %d ms | Total Space = %.3f MB%n",
                totalBFTime, totalBFSpace / 1_000_000.0);
        System.out.println("==========================================");

        // Step 7: Print LCS matrix if requested
        if (showMatrix) {
            OutputFormatter.printLCSMatrix(inputSequences, dynamicResults);
        }
    }
}
