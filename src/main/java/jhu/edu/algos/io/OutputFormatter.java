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
 * including metrics, DP table (for dynamic), and a final summary table.
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

            dualPrint(writer, "=".repeat(70));
            dualPrint(writer, "Number of sequences to be compared: " + inputSequences.size());

            int index = 1;
            for (Map.Entry<String, String> entry : inputSequences.entrySet()) {
                dualPrint(writer, "Sequence #" + index + " | Length: " + entry.getValue().length());
                dualPrint(writer, entry.getValue());
                index++;
            }
            dualPrint(writer, "=".repeat(70));

            for (int i = 0; i < dynamicResults.size(); i++) {
                LCSResult dyn = dynamicResults.get(i);
                LCSResult bf = bruteForceResults.get(i);

                dualPrint(writer, "\nComparing sequences " + dyn.getComparisonLabel());
                dualPrint(writer, "Longest common subsequence | Length: " + dyn.getLCSLength());
                dualPrint(writer, dyn.getLCS());

                // Print the matrix
                dualPrint(writer, "\nPrinting out subsequence matrix...");
                printMatrix(writer, dyn.getFirstInput(), dyn.getSecondInput());

                // Dynamic block
                dualPrint(writer, "\n-- Dynamic Programming LCS --");
                printResultBlock(writer, dyn);

                // Brute-force block
                dualPrint(writer, "\n-- Brute Force LCS --");
                printResultBlock(writer, bf);

                dualPrint(writer, "\n" + "=".repeat(70));
            }

            // Summary
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
            PerformanceMetrics dm = dyn.getMetrics();
            PerformanceMetrics bm = bf.getMetrics();

            dualPrint(writer, String.format(
                    "%-12s | %-10d %-12d | %-10d %-12d | %d",
                    dyn.getComparisonLabel(),
                    dm.getComparisonCount(), dm.getElapsedTimeMs(),
                    bm.getComparisonCount(), bm.getElapsedTimeMs(),
                    dyn.getLCSLength()
            ));
        }

        dualPrint(writer, "=".repeat(70));
    }

    /**
     * Prints the DP matrix used to compute the LCS between two sequences.
     * This is a visual representation of the computation table.
     *
     * @param writer PrintWriter for output
     * @param s1     First sequence
     * @param s2     Second sequence
     */
    private static void printMatrix(PrintWriter writer, String s1, String s2) {
        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        // Fill the matrix
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // Print header
        dualPrint(writer, "\t  " + String.join(" ", s2.split("")));
        for (int i = 0; i <= m; i++) {
            StringBuilder row = new StringBuilder();
            if (i == 0) row.append("  ");
            else row.append(s1.charAt(i - 1)).append(" ");
            for (int j = 0; j <= n; j++) {
                row.append(dp[i][j]).append(" ");
            }
            dualPrint(writer, row.toString().trim());
        }
    }

    private static void dualPrint(PrintWriter writer, String line) {
        System.out.println(line);      // Print to terminal
        writer.println(line);          // Print to file
    }
}
