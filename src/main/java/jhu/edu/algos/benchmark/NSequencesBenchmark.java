package jhu.edu.algos.benchmark;

import jhu.edu.algos.lcs.*;
import jhu.edu.algos.graph.NSequencesGraphGenerator;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * NSequencesBenchmark:
 * Fixes sequence length (L), and scales number of sequences (N).
 *
 * For each N, all pairwise comparisons (C(N, 2)) are performed using both
 * Dynamic Programming and Brute Force LCS algorithms. This evaluates scalability
 * with respect to the number of comparisons as N increases.
 *
 * Useful for assessing the impact of data volume (N) on total comparisons and runtime.
 */
public class NSequencesBenchmark {

    // Fixed sequence length (L) for every sequence
    private static final int FIXED_LENGTH = 10;

    // Values of N to test (number of total sequences in the dataset)
    private static final int[] SEQUENCE_COUNTS = {4, 6, 8, 10, 20, 40};

    // Brute force will be skipped if L exceeds this value
    private static final int BRUTE_FORCE_CAP = 30;

    // Output file paths
    private static final String DEFAULT_TXT = "nsequences_benchmark.txt";
    private static final String DEFAULT_PNG = "nsequences_plot.png";

    /**
     * Entry point for running the benchmark.
     * Supports optional --plot flag to generate PNG graph.
     */
    public static void main(String[] args) {
        boolean plot = Arrays.asList(args).contains("--plot");

        try {
            run(DEFAULT_TXT, DEFAULT_PNG, plot);
        } catch (Throwable t) {
            System.err.println("Benchmark failed: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            System.exit(1);
        }
    }

    /**
     * Runs the benchmark across increasing values of N (number of sequences).
     * Computes all pairwise LCS comparisons and records total metrics.
     *
     * @param txtPath  Path to output text file
     * @param pngPath  Path to output PNG plot
     * @param makePlot Whether to generate a graph
     */
    public static void run(String txtPath, String pngPath, boolean makePlot) throws Exception {
        AbstractLCS dyn = new LCSDynamic();
        AbstractLCS brute = new LCSBruteForce();
        Random rand = new Random();

        List<LCSResult> dynResults = new ArrayList<>();
        List<LCSResult> bruteResults = new ArrayList<>();

        try (PrintWriter out = new PrintWriter(new FileWriter(txtPath))) {
            // Output report header
            out.println("N-Sequences Benchmark Report");
            out.println("Fixed sequence length (L): " + FIXED_LENGTH);
            out.println("Evaluating scalability by number of sequences (N)");
            out.println("=".repeat(95));
            out.printf("%-12s | %-20s %-12s | %-20s %-12s%n",
                    "Sequences", "Dyn_Comparisons", "Dyn_Time", "BF_Comparisons", "BF_Time");
            out.println("-".repeat(95));

            // Iterate over increasing N
            for (int count : SEQUENCE_COUNTS) {
                List<String> seqs = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    seqs.add(generateDNA(rand, FIXED_LENGTH));
                }

                long dynTotalTime = 0;
                long dynTotalComp = 0;
                long bfTotalTime = 0;
                long bfTotalComp = 0;

                // All pairwise comparisons for this N
                for (int i = 0; i < count; i++) {
                    for (int j = i + 1; j < count; j++) {
                        String s1 = seqs.get(i);
                        String s2 = seqs.get(j);
                        String label = "S" + (i + 1) + "_vs_S" + (j + 1);

                        // Dynamic LCS
                        LCSResult dynResult = dyn.computeLCS(label, s1, s2);
                        dynResults.add(dynResult);
                        dynTotalTime += dynResult.getMetrics().getElapsedTimeMs();
                        dynTotalComp += dynResult.getMetrics().getComparisonCount();

                        // Brute Force LCS (if within safe range)
                        if (FIXED_LENGTH <= BRUTE_FORCE_CAP) {
                            LCSResult bfResult = brute.computeLCS(label, s1, s2);
                            bruteResults.add(bfResult);
                            bfTotalTime += bfResult.getMetrics().getElapsedTimeMs();
                            bfTotalComp += bfResult.getMetrics().getComparisonCount();
                        } else {
                            bruteResults.add(null); // Mark skipped
                        }
                    }
                }

                // Write summary row for current N
                out.printf("%-12d | %-20d %-12d | %-20s %-12s%n",
                        count,
                        dynTotalComp, dynTotalTime,
                        (FIXED_LENGTH <= BRUTE_FORCE_CAP) ? bfTotalComp : "-",
                        (FIXED_LENGTH <= BRUTE_FORCE_CAP) ? bfTotalTime : "-"
                );
            }

            out.println("=".repeat(95));
            System.out.println(" Benchmark complete. TXT written to: " + txtPath);

            if (makePlot) {
                System.out.println(" Generating plot...");
                NSequencesGraphGenerator.generateGraph(dynResults, bruteResults, pngPath);
            }
        }
    }

    /**
     * Utility function to generate a random DNA sequence of fixed length.
     *
     * @param rand Random number generator
     * @param len  Length of the DNA sequence
     * @return DNA string of specified length
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
