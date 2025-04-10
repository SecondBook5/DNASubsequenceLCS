package jhu.edu.algos;

import jhu.edu.algos.lcs.*;
import jhu.edu.algos.utils.GraphGenerator;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * ScalingBenchmark runs the Dynamic Programming and Brute Force LCS algorithms
 * on a set of increasing sequence sizes to collect asymptotic performance data.
 * Brute-force is skipped for sizes above the threshold.
 */
public class ScalingBenchmark {

    // Sequence length increments
    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 100;
    private static final int STEP_SIZE = 10;

    // Maximum size for running brute-force safely
    private static final int BRUTE_FORCE_LIMIT = 30;

    // Default output locations
    private static final String DEFAULT_TXT = "scaling_benchmark.txt";
    private static final String DEFAULT_PNG = "scaling_plot.png";

    /**
     * CLI entry point. Accepts "--plot" flag to trigger PNG plot generation.
     */
    public static void main(String[] args) {
        boolean generatePlot = Arrays.asList(args).contains("--plot");
        try {
            run(DEFAULT_TXT, DEFAULT_PNG, generatePlot);
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

            System.exit(1);
        }
    }

    /**
     * Executes the scaling benchmark, writing results to TXT and optionally plotting a graph.
     *
     * @param txtPath  Path to output report file (.txt)
     * @param plotPath Path to save plot (.png)
     * @param makePlot Whether to generate the graph
     */
    public static void run(String txtPath, String plotPath, boolean makePlot) throws Exception {
        List<LCSResult> dynamicResults = new ArrayList<>();
        List<LCSResult> bruteResults = new ArrayList<>();

        AbstractLCS dyn = new LCSDynamic();
        AbstractLCS brute = new LCSBruteForce();

        try (PrintWriter out = new PrintWriter(new FileWriter(txtPath))) {
            out.println("Scaling Benchmark Report");
            out.println("=".repeat(80));
            out.printf("%-10s | %-15s %-12s | %-15s %-12s%n", "Length", "Dyn_Comparisons", "Dyn_Time(ms)", "BF_Comparisons", "BF_Time(ms)");
            out.println("-".repeat(80));

            for (int len = MIN_LENGTH; len <= MAX_LENGTH; len += STEP_SIZE) {
                String s1 = generateSequence(len);
                String s2 = generateSequence(len);
                String label = len + "x" + len;

                System.out.printf("Running benchmark for input size: %s%n", label);

                LCSResult dynResult = dyn.computeLCS(label, s1, s2);
                dynamicResults.add(dynResult);

                LCSResult bruteResult = null;
                if (len <= BRUTE_FORCE_LIMIT) {
                    bruteResult = brute.computeLCS(label, s1, s2);
                }

                // Always add result or null to preserve label order
                bruteResults.add(bruteResult);

                out.printf("%-10d | %-15d %-12d | %-15s %-12s%n",
                        len,
                        dynResult.getMetrics().getComparisonCount(),
                        dynResult.getMetrics().getElapsedTimeMs(),
                        bruteResult != null ? bruteResult.getMetrics().getComparisonCount() : "-",
                        bruteResult != null ? bruteResult.getMetrics().getElapsedTimeMs() : "-"
                );
            }

            out.println("=".repeat(80));
            System.out.println(" Benchmark complete. TXT written to: " + txtPath);

            if (makePlot) {
                System.out.println(" Generating graph...");
                GraphGenerator.generateGraph(dynamicResults, bruteResults, plotPath);
            }
        }
    }

    /**
     * Generates a synthetic DNA sequence of the given length (A, C, G, T repeated).
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
