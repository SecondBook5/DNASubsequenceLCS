package jhu.edu.algos.io;

import jhu.edu.algos.lcs.LCSResult;
import jhu.edu.algos.utils.PerformanceMetrics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * OutputFormatter handles the display and writing of LCS results
 * to both the console and a file. Results are printed per pair,
 * including metrics, and a final summary table is appended.
 */
public class OutputFormatter {

    /**
     * Writes LCS results and metrics to both terminal and output file.
     *
     * @param inputSequences    The original input sequences (labeled as S1, S2, etc.)
     * @param dynamicResults    List of LCSResults computed using the dynamic algorithm.
     * @param bruteForceResults List of LCSResults computed using the brute-force algorithm.
     * @param outputPath        Path to the file where the results should be saved.
     */
    public static void writeResults(
            Map<String, String> inputSequences,
            List<LCSResult> dynamicResults,
            List<LCSResult> bruteForceResults,
            String outputPath
    ) {
        try {
            // Create a writer to the output file
            PrintWriter writer = new PrintWriter(new FileWriter(outputPath));

            // Use a helper method to print to both terminal and file
            dualPrint(writer, "======================================================================");
            dualPrint(writer, "Input Sequences:");

            // Echo the original input sequences
            for (Map.Entry<String, String> entry : inputSequences.entrySet()) {
                dualPrint(writer, entry.getKey() + " = " + entry.getValue());
            }

            dualPrint(writer, "======================================================================");

            // Iterate through all results by index (assuming results are paired in same order)
            for (int i = 0; i < dynamicResults.size(); i++) {
                LCSResult dyn = dynamicResults.get(i);
                LCSResult bf = bruteForceResults.get(i);

                dualPrint(writer, "\nPairwise Comparison: " + dyn.getComparisonLabel());

                // -- Dynamic Result Block --
                dualPrint(writer, "\n-- Dynamic Programming LCS --");
                printResultBlock(writer, dyn);

                // -- Brute Force Result Block --
                dualPrint(writer, "\n-- Brute Force LCS --");
                printResultBlock(writer, bf);

                dualPrint(writer, "\n" + "=".repeat(70));
            }

            // Print the final summary table
            printSummaryTable(writer, dynamicResults, bruteForceResults);

            // Close the writer after finishing
            writer.close();

        } catch (IOException e) {
            System.err.println("Error writing results to file: " + e.getMessage());
        }
    }

    /**
     * Prints a single LCS result block to both console and file.
     *
     * @param writer PrintWriter for file output
     * @param result LCSResult object to be printed
     */
    private static void printResultBlock(PrintWriter writer, LCSResult result) {
        PerformanceMetrics metrics = result.getMetrics();

        dualPrint(writer, "LCS        : " + result.getLCS());
        dualPrint(writer, "Length     : " + result.getLCSLength());
        dualPrint(writer, "Comparisons: " + metrics.getComparisonCount());
        dualPrint(writer, "Time (ms)  : " + metrics.getElapsedTimeMs());
    }

    /**
     * Prints a final summary table comparing metrics of both algorithms.
     *
     * @param writer PrintWriter for file output
     * @param dynamic List of results from dynamic algorithm
     * @param bruteForce List of results from brute-force algorithm
     */
    private static void printSummaryTable(PrintWriter writer,
                                          List<LCSResult> dynamic,
                                          List<LCSResult> bruteForce) {

        dualPrint(writer, "\nSummary Table (Comparisons and Time)");
        dualPrint(writer, "=".repeat(70));
        dualPrint(writer, String.format(
                "%-12s | %-10s %-12s | %-10s %-12s | %s",
                "Pair", "Dyn_Comp", "Dyn_Time(ms)", "BF_Comp", "BF_Time(ms)", "LCS_Len"));
        dualPrint(writer, "-".repeat(70));

        for (int i = 0; i < dynamic.size(); i++) {
            LCSResult dyn = dynamic.get(i);
            LCSResult bf = bruteForce.get(i);

            String label = dyn.getComparisonLabel();
            PerformanceMetrics dm = dyn.getMetrics();
            PerformanceMetrics bm = bf.getMetrics();

            dualPrint(writer, String.format(
                    "%-12s | %-10d %-12d | %-10d %-12d | %d",
                    label,
                    dm.getComparisonCount(), dm.getElapsedTimeMs(),
                    bm.getComparisonCount(), bm.getElapsedTimeMs(),
                    dyn.getLCSLength()
            ));
        }

        dualPrint(writer, "=".repeat(70));
    }

    /**
     * Helper method to print to both console and file.
     *
     * @param writer The PrintWriter for file output
     * @param line   The string line to print
     */
    private static void dualPrint(PrintWriter writer, String line) {
        System.out.println(line);      // Print to terminal
        writer.println(line);          // Print to file
    }
}
