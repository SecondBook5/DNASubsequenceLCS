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
                LCSResult bf = bruteForceResults.get(i); // May be null

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
                if (bf != null) {
                    printResultBlock(writer, bf);
                } else {
                    dualPrint(writer, "SKIPPED: Brute-force LCS was not computed due to length cap.");
                }

                dualPrint(writer, "\n" + "=".repeat(70));
            }

            // Print final summary table
            printSummaryTable(writer, dynamicResults, bruteForceResults);

            // Close the writer after finishing
            writer.close();

        } catch (IOException e) {
            System.err.println("Error writing results to file: " + e.getMessage());
        }
    }

    /**
     * Prints a single result block of LCS stats to both file and terminal.
     *
     * @param writer PrintWriter for file
     * @param result LCSResult object
     */
    private static void printResultBlock(PrintWriter writer, LCSResult result) {
        PerformanceMetrics metrics = result.getMetrics();
        dualPrint(writer, "LCS        : " + result.getLCS());
        dualPrint(writer, "Length     : " + result.getLCSLength());
        dualPrint(writer, String.format("Comparisons: %,d", metrics.getComparisonCount()));
        dualPrint(writer, String.format("Time (ms)  : %.4e", metrics.getElapsedTimeMs() * 1.0));
        dualPrint(writer, String.format("Space (MB) : %.4e", metrics.getEstimatedSpaceMB()));
    }

    /**
     * Prints a final summary table of all pairwise comparisons.
     *
     * @param writer     PrintWriter
     * @param dynamic    List of results from dynamic programming
     * @param bruteForce List of results from brute-force (some may be null)
     */
    private static void printSummaryTable(PrintWriter writer,
                                          List<LCSResult> dynamic,
                                          List<LCSResult> bruteForce) {
        dualPrint(writer, "\nSummary Table (Comparisons, Time, and Space)");
        dualPrint(writer, "=".repeat(70));
        dualPrint(writer, String.format(
                "%-16s | %-12s %-12s %-12s | %-12s %-12s %-12s | %s",
                "Pair",
                "Dyn_Comp", "Dyn_Time", "Dyn_MB",
                "BF_Comp", "BF_Time", "BF_MB",
                "LCS_Len"));
        dualPrint(writer, "-".repeat(70));

        for (int i = 0; i < dynamic.size(); i++) {
            LCSResult dyn = dynamic.get(i);
            LCSResult bf = bruteForce.get(i);
            PerformanceMetrics dm = dyn.getMetrics();

            String bfComp = "-";
            String bfTime = "-";
            String bfSpace = "-";

            if (bf != null) {
                PerformanceMetrics bm = bf.getMetrics();
                bfComp = String.format("%,12d", bm.getComparisonCount());
                bfTime = String.format("%.4e", bm.getElapsedTimeMs() * 1.0);
                bfSpace = String.format("%.4e", bm.getEstimatedSpaceMB());
            }

            dualPrint(writer, String.format(
                    "%-16s | %,12d %.4e %.4e | %12s %12s %12s | %d",
                    dyn.getComparisonLabel(),
                    dm.getComparisonCount(), dm.getElapsedTimeMs() * 1.0, dm.getEstimatedSpaceMB(),
                    bfComp, bfTime, bfSpace,
                    dyn.getLCSLength()
            ));
        }

        dualPrint(writer, "=".repeat(70));
    }

    /**
     * Prints the DP matrix used for LCS computation.
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

    /**
     * Utility method to write a line to both file and terminal.
     */
    private static void dualPrint(PrintWriter writer, String line) {
        System.out.println(line);
        writer.println(line);
    }

    /**
     * Appends final performance totals to the output file.
     */
    public static void appendPerformanceSummary(long dynTime, long dynSpace,
                                                long bfTime, long bfSpace,
                                                String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile, true))) {
            writer.println();
            writer.println("===== Aggregate Performance Summary =====");
            writer.printf("Dynamic Programming: Total Time = %d ms | Total Space = %.3f MB%n",
                    dynTime, dynSpace / 1_000_000.0);
            writer.printf("Brute Force        : Total Time = %d ms | Total Space = %.3f MB%n",
                    bfTime, bfSpace / 1_000_000.0);
            writer.println("==========================================");
        } catch (IOException e) {
            System.err.println("Error appending performance summary to file: " + e.getMessage());
        }
    }

    /**
     * Prints the pairwise LCS length matrix using only dynamic results.
     */
    public static void printLCSMatrix(Map<String, String> inputSequences, List<LCSResult> dynamicResults) {
        System.out.println("\n===== LCS Length Matrix (Dynamic Programming) =====");

        List<String> labels = new ArrayList<>(inputSequences.keySet());
        int n = labels.size();

        // Create matrix to hold LCS lengths
        String[][] matrix = new String[n][n];

        // Fill matrix with results
        int index = 0;

        for (int i = 0; i < n; i++) {
            matrix[i][i] = "--"; // self comparison
            for (int j = i + 1; j < n; j++) {
                if (index < dynamicResults.size()) {
                    String len = String.valueOf(dynamicResults.get(index++).getLCSLength());
                    matrix[i][j] = len;
                    matrix[j][i] = len;
                } else {
                    matrix[i][j] = matrix[j][i] = "??";
                }
            }
        }

        // Print header
        System.out.printf("%-6s", "");
        for (String col : labels) System.out.printf("%-8s", col);
        System.out.println();

        // Print each row
        for (int i = 0; i < n; i++) {
            System.out.printf("%-6s", labels.get(i));
            for (int j = 0; j < n; j++) {
                System.out.printf("%-8s", matrix[i][j] == null ? "" : matrix[i][j]);
            }
            System.out.println();
        }

        System.out.println("===================================================\n");
    }
}
