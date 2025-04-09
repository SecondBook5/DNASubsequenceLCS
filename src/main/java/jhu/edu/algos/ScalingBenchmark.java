package jhu.edu.algos;

import jhu.edu.algos.lcs.*;
import jhu.edu.algos.utils.GraphGenerator;
import jhu.edu.algos.utils.PerformanceMetrics;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * ScalingBenchmark runs the Dynamic Programming and Brute Force LCS algorithms
 * on a set of increasing sequence sizes to collect asymptotic performance data.
 *
 * This is used to analyze and compare empirical time and comparison counts with
 * theoretical expectations. Brute-force is only applied up to a safe threshold.
 */
public class ScalingBenchmark {

    // Sequence length increments
    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 100;
    private static final int STEP_SIZE = 10;

    // Maximum size for running brute-force safely
    private static final int BRUTE_FORCE_LIMIT = 30;

    // Output locations
    private static final String CSV_PATH = "scaling_benchmark.csv";
    private static final String PLOT_PATH = "scaling_plot.png";

    /**
     * Main benchmark driver. Run with --plot to generate a PNG graph.
     *
     * @param args Command-line args (e.g., --plot)
     */
    public static void main(String[] args) {
        boolean generatePlot = Arrays.asList(args).contains("--plot");

        List<LCSResult> dynamicResults = new ArrayList<>();
        List<LCSResult> bruteResults = new ArrayList<>();

        AbstractLCS dyn = new LCSDynamic();
        AbstractLCS brute = new LCSBruteForce();

        try (PrintWriter csv = new PrintWriter(new FileWriter(CSV_PATH))) {

            // Write CSV header
            csv.println("Length,Dynamic_Comparisons,Dynamic_Time_ms,Brute_Comparisons,Brute_Time_ms");

            for (int len = MIN_LENGTH; len <= MAX_LENGTH; len += STEP_SIZE) {
                String s1 = generateSequence(len);
                String s2 = generateSequence(len);
                String label = len + "x" + len;

                System.out.printf("Running benchmark for input size: %s%n", label);

                // Run Dynamic Programming
                LCSResult dynResult = dyn.computeLCS(label, s1, s2);
                dynamicResults.add(dynResult);

                // Run Brute Force (only for small inputs)
                LCSResult bruteResult = null;
                if (len <= BRUTE_FORCE_LIMIT) {
                    bruteResult = brute.computeLCS(label, s1, s2);
                    bruteResults.add(bruteResult);
                }

                // Write result to CSV
                csv.printf(
                        "%d,%d,%d,%s,%s%n",
                        len,
                        dynResult.getMetrics().getComparisonCount(),
                        dynResult.getMetrics().getElapsedTimeMs(),
                        bruteResult != null ? bruteResult.getMetrics().getComparisonCount() : "",
                        bruteResult != null ? bruteResult.getMetrics().getElapsedTimeMs() : ""
                );
            }

            System.out.println(" Benchmark complete. CSV written to: " + CSV_PATH);

            if (generatePlot) {
                System.out.println(" Generating graph...");
                GraphGenerator.generateGraph(dynamicResults, bruteResults, PLOT_PATH);
            }

        } catch (Throwable t) {
            System.err.println(" Benchmark execution failed due to an unexpected error.");
            System.err.println("→ Reason: " + t.getClass().getSimpleName() + " - " + t.getMessage());

            if (t instanceof OutOfMemoryError) {
                System.err.println(" Tip: You may have exceeded JVM heap limits. Lower BRUTE_FORCE_LIMIT or increase heap.");
            } else if (t instanceof java.io.IOException) {
                System.err.println(" Tip: Check disk write access, filename, or space.");
            }

            StackTraceElement[] stack = t.getStackTrace();
            System.err.println("→ Location: " + stack[0]);
            if (stack.length > 1) System.err.println("             " + stack[1]);
            if (stack.length > 2) System.err.println("             " + stack[2]);

            System.exit(1); // Exit with failure code
        }
    }

    /**
     * Generates a mock DNA sequence by cycling through A, C, G, T.
     *
     * @param length Desired sequence length
     * @return Synthetic DNA string
     */
    private static String generateSequence(int length) {
        StringBuilder sb = new StringBuilder(length);
        char[] bases = {'A', 'C', 'G', 'T'};
        for (int i = 0; i < length; i++) {
            sb.append(bases[i % 4]);
        }
        return sb.toString();
    }
}
