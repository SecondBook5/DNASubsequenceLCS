package jhu.edu.algos.benchmark;

import jhu.edu.algos.lcs.*;
import jhu.edu.algos.graph.ScalingGraphGenerator;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * Hybrid ScalingBenchmark: Combines increasing sequence length with multiple pairwise comparisons.
 * For each length tier, runs several pairwise LCS computations and records performance metrics.
 * Brute force is capped beyond a safe limit and skipped to prevent runtime blowup.
 */
public class ScalingBenchmark {

    // Range of lengths to evaluate (stepwise)
    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 60;
    private static final int STEP = 10;

    // Number of pairs per length
    private static final int PAIRS_PER_LENGTH = 5;

    // Max length where brute force is safely allowed
    private static final int BRUTE_FORCE_CAP = 30;

    // Output paths
    private static final String DEFAULT_TXT = "scaling_benchmark.txt";
    private static final String DEFAULT_PNG = "scaling_plot.png";

    /**
     * CLI entry point. Supports:
     * --run       Run full benchmark (default if no flag)
     * --plot      Generate PNG plot
     * --test      Run short version for unit testing
     */
    public static void main(String[] args) {
        Set<String> argSet = new HashSet<>(Arrays.asList(args));
        boolean runTest = argSet.contains("--test");
        boolean runFull = argSet.contains("--run");
        boolean makePlot = argSet.contains("--plot");

        try {
            if (runTest) {
                System.out.println("Running lightweight test benchmark...");
                run("test_scaling.txt", "test_scaling.png", makePlot, 10, 20, 10, 2);
            } else if (runFull || args.length == 0) {
                System.out.println("Running full scaling benchmark...");
                run(DEFAULT_TXT, DEFAULT_PNG, makePlot);
            } else {
                System.out.println("""
                    Unknown or missing flags. Usage:
                      --run         Run full benchmark (default if no flag)
                      --plot        Generate PNG plot
                      --test        Run short version for unit testing

                    Example:
                      java ScalingBenchmark --run --plot
                      java ScalingBenchmark --test
                    """);
            }
        } catch (Throwable t) {
            System.err.println("Benchmark failed: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            if (t instanceof OutOfMemoryError) {
                System.err.println("→ Try lowering BRUTE_FORCE_CAP or increasing JVM heap.");
            }
            System.exit(1);
        }
    }

    /**
     * Runs the benchmark across multiple lengths and pairs.
     *
     * @param txtPath   Output results file
     * @param plotPath  Output PNG file
     * @param makePlot  Whether to generate a graph
     */
    public static void run(String txtPath, String plotPath, boolean makePlot) throws Exception {
        run(txtPath, plotPath, makePlot, MIN_LENGTH, MAX_LENGTH, STEP, PAIRS_PER_LENGTH);
    }

    /**
     * Overloaded version that accepts custom parameters for testing.
     *
     * @param txtPath         Output results file
     * @param plotPath        Output PNG file
     * @param makePlot        Whether to generate a graph
     * @param minLen          Minimum sequence length
     * @param maxLen          Maximum sequence length
     * @param step            Step size for length
     * @param pairsPerLength  Number of sequence pairs per length
     */
    public static void run(String txtPath, String plotPath, boolean makePlot,
                           int minLen, int maxLen, int step, int pairsPerLength) throws Exception {

        List<LCSResult> dynamicResults = new ArrayList<>();
        List<LCSResult> bruteResults = new ArrayList<>();

        AbstractLCS dyn = new LCSDynamic();
        AbstractLCS brute = new LCSBruteForce();
        Random rand = new Random();

        try (PrintWriter out = new PrintWriter(new FileWriter(txtPath))) {
            // Updated header for test compatibility
            out.println("Scaling Benchmark Report");
            out.println("Hybrid Scaling Benchmark: Varying Lengths and Pair Counts");
            out.println("=".repeat(100));
            out.printf("%-12s | %-10s | %-15s %-12s | %-15s %-12s%n",
                    "Length", "Pair", "Dyn_Comparisons", "Dyn_Time(ms)", "BF_Comparisons", "BF_Time(ms)");
            out.println("-".repeat(100));

            for (int len = minLen; len <= maxLen; len += step) {
                for (int i = 1; i <= pairsPerLength; i++) {
                    String s1 = generateDNA(rand, len);
                    String s2 = generateDNA(rand, len);
                    String label = "L" + len + "_P" + i;

                    System.out.printf("→ %s%n", label);

                    LCSResult dynResult = dyn.computeLCS(label, s1, s2);
                    dynamicResults.add(dynResult);

                    LCSResult bruteResult = null;
                    if (len <= BRUTE_FORCE_CAP) {
                        bruteResult = brute.computeLCS(label, s1, s2);
                    }

                    bruteResults.add(bruteResult); // may be null

                    out.printf("%-12d | %-10s | %-15d %-12d | %-15s %-12s%n",
                            len,
                            "Pair_" + i,
                            dynResult.getMetrics().getComparisonCount(),
                            dynResult.getMetrics().getElapsedTimeMs(),
                            bruteResult != null ? bruteResult.getMetrics().getComparisonCount() : "-",
                            bruteResult != null ? bruteResult.getMetrics().getElapsedTimeMs() : "-"
                    );
                }
            }

            out.println("=".repeat(100));
            System.out.println(" Benchmark complete. TXT written to: " + txtPath);

            if (makePlot) {
                System.out.println(" Generating graph...");
                ScalingGraphGenerator.generateGraph(dynamicResults, bruteResults, plotPath);
            }
        }
    }

    /**
     * Generates a random DNA string of fixed length.
     */
    private static String generateDNA(Random rand, int len) {
        char[] bases = {'A', 'C', 'G', 'T'};
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(bases[rand.nextInt(4)]);
        }
        return sb.toString();
    }
}
