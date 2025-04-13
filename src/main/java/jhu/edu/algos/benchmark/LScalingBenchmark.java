package jhu.edu.algos.benchmark;

import jhu.edu.algos.lcs.*;
import jhu.edu.algos.graph.LScalingGraphGenerator;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * LScalingBenchmark:
 * Evaluates LCS algorithm performance by increasing sequence length (L),
 * while keeping the number of sequence pairs (N) constant for each length tier.
 *
 * For each length tier (e.g., L = 10, 20, ..., 60), N sequence pairs are generated.
 * The actual lengths are randomized within a bucket (e.g., L=20 means [20,29]).
 * This simulates biological input variability while enabling controlled benchmarking.
 *
 * Both Dynamic Programming and Brute Force algorithms are applied.
 * Brute force is skipped beyond BRUTE_FORCE_CAP to avoid runtime explosion.
 */
public class LScalingBenchmark {

    // Minimum and maximum base sequence lengths (buckets will range L to L+9)
    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 60;
    private static final int STEP = 10;

    // Number of random sequence pairs (N) to generate per L bucket
    private static final int PAIRS_PER_LENGTH = 5;

    // Brute force will only be computed if L <= this threshold
    private static final int BRUTE_FORCE_CAP = 30;

    // Output filenames
    private static final String DEFAULT_TXT = "scaling_benchmark.txt";
    private static final String DEFAULT_PNG = "scaling_plot.png";

    /**
     * Entry point to run benchmark from CLI.
     * Supports flags:
     *   --run    Run full benchmark (default if no args)
     *   --plot   Also generate PNG graph
     *   --test   Run smaller test version
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
                System.out.println("Running full L-scaling benchmark...");
                run(DEFAULT_TXT, DEFAULT_PNG, makePlot);
            } else {
                System.out.println("""
          Unknown or missing flags. Usage:
            --run         Run full benchmark (default if no flag)
            --plot        Generate PNG plot
            --test        Run short version for unit testing

          Example:
            java LScalingBenchmark --run --plot
            java LScalingBenchmark --test
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
     * Default run method using full configuration range.
     */
    public static void run(String txtPath, String pngPath, boolean makePlot) throws Exception {
        run(txtPath, pngPath, makePlot, MIN_LENGTH, MAX_LENGTH, STEP, PAIRS_PER_LENGTH);
    }

    /**
     * Core run method with configurable parameters.
     * For each L bucket, generates N random sequence pairs and computes LCS metrics.
     */
    public static void run(String txtPath, String pngPath, boolean makePlot,
                           int minLen, int maxLen, int step, int pairsPerLength) throws Exception {

        List<LCSResult> dynamicResults = new ArrayList<>();
        List<LCSResult> bruteResults = new ArrayList<>();

        AbstractLCS dyn = new LCSDynamic();
        AbstractLCS brute = new LCSBruteForce();
        Random rand = new Random();

        try (PrintWriter out = new PrintWriter(new FileWriter(txtPath))) {
            // Header for results
            out.println("L-Scaling Benchmark Report");
            out.println("Varying Sequence Length (L) while keeping Pair Count (N=" + pairsPerLength + ") fixed");
            out.println("=".repeat(100));
            out.printf("%-12s | %-10s | %-15s %-12s | %-15s %-12s%n",
                    "Length", "Pair", "Dyn_Comparisons", "Dyn_Time(ms)", "BF_Comparisons", "BF_Time(ms)");
            out.println("-".repeat(100));

            // Loop over each length tier
            for (int baseLen = minLen; baseLen <= maxLen; baseLen += step) {
                for (int i = 1; i <= pairsPerLength; i++) {
                    // Add variability: actual length is in [L, L+9]
                    int len1 = baseLen + rand.nextInt(10);
                    int len2 = baseLen + rand.nextInt(10);
                    String s1 = generateDNA(rand, len1);
                    String s2 = generateDNA(rand, len2);
                    String label = "L" + baseLen + "_P" + i;

                    System.out.printf("→ %s%n", label);

                    // Dynamic LCS
                    LCSResult dynResult = dyn.computeLCS(label, s1, s2);
                    dynamicResults.add(dynResult);

                    // Brute Force (only if L is safe)
                    LCSResult bruteResult = null;
                    if (baseLen <= BRUTE_FORCE_CAP) {
                        bruteResult = brute.computeLCS(label, s1, s2);
                    }

                    bruteResults.add(bruteResult); // may be null if skipped

                    // Write results to output
                    out.printf("%-12d | %-10s | %-15d %-12d | %-15s %-12s%n",
                            baseLen,
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
                LScalingGraphGenerator.generateGraph(dynamicResults, bruteResults, pngPath);
            }
        }
    }

    /**
     * Generates a random DNA string of specified length.
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
