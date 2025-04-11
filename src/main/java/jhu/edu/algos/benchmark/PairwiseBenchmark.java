package jhu.edu.algos.benchmark;

import jhu.edu.algos.lcs.*;
import jhu.edu.algos.graph.PairwiseGraphGenerator;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * PairwiseBenchmark:
 * Fixes sequence length, increases number of pairwise comparisons.
 * Tracks cumulative comparison count and time.
 */
public class PairwiseBenchmark {

    private static final int FIXED_LENGTH = 10;
    private static final int MAX_PAIRS = 50;
    private static final int STEP = 5;
    private static final int BRUTE_FORCE_CAP = 30;

    private static final String DEFAULT_TXT = "pairwise_benchmark.txt";
    private static final String DEFAULT_PNG = "pairwise_plot.png";

    /**
     * CLI entry point. Accepts:
     * --plot → to generate PNG
     * unknown args are ignored gracefully
     */
    public static void main(String[] args) {
        boolean plot = Arrays.asList(args).contains("--plot");

        try {
            run(DEFAULT_TXT, DEFAULT_PNG, plot, FIXED_LENGTH, MAX_PAIRS, STEP);
        } catch (Throwable t) {
            System.err.println("Benchmark failed: " + t.getClass().getSimpleName() + " - " + t.getMessage());
            System.exit(1);
        }
    }

    /**
     * Runs the pairwise benchmark with user-defined parameters.
     *
     * @param txtPath   output file path (.txt)
     * @param pngPath   output plot path (.png)
     * @param makePlot  whether to generate PNG
     * @param length    fixed sequence length
     * @param maxPairs  total pair count limit
     * @param step      increment in number of pairs
     */
    public static void run(String txtPath, String pngPath, boolean makePlot,
                           int length, int maxPairs, int step) throws Exception {

        AbstractLCS dyn = new LCSDynamic();
        AbstractLCS brute = new LCSBruteForce();
        Random rand = new Random();

        List<LCSResult> dynResults = new ArrayList<>();
        List<LCSResult> bruteResults = new ArrayList<>();

        try (PrintWriter out = new PrintWriter(new FileWriter(txtPath))) {
            out.println("Pairwise Benchmark Report");
            out.println("Fixed sequence length: " + length);
            out.println("=".repeat(90));
            out.printf("%-12s | %-18s %-12s | %-18s %-12s%n",
                    "Pairs", "Dyn_Comparisons", "Dyn_Time", "BF_Comparisons", "BF_Time");
            out.println("-".repeat(90));

            for (int count = step; count <= maxPairs; count += step) {
                long dynTotalTime = 0;
                long dynTotalComp = 0;
                long bfTotalTime = 0;
                long bfTotalComp = 0;

                for (int i = 0; i < count; i++) {
                    String s1 = generateDNA(rand, length);
                    String s2 = generateDNA(rand, length);
                    String label = "P" + count + "_R" + i;

                    LCSResult dynResult = dyn.computeLCS(label, s1, s2);
                    dynResults.add(dynResult);
                    dynTotalTime += dynResult.getMetrics().getElapsedTimeMs();
                    dynTotalComp += dynResult.getMetrics().getComparisonCount();

                    if (length <= BRUTE_FORCE_CAP) {
                        LCSResult bfResult = brute.computeLCS(label, s1, s2);
                        bruteResults.add(bfResult);
                        bfTotalTime += bfResult.getMetrics().getElapsedTimeMs();
                        bfTotalComp += bfResult.getMetrics().getComparisonCount();
                    } else {
                        bruteResults.add(null); // no BF results if above cap
                    }
                }

                out.printf("%-12d | %-18d %-12d | %-18s %-12s%n",
                        count,
                        dynTotalComp, dynTotalTime,
                        (length <= BRUTE_FORCE_CAP) ? bfTotalComp : "-",
                        (length <= BRUTE_FORCE_CAP) ? bfTotalTime : "-"
                );
            }

            out.println("=".repeat(90));
            System.out.println(" Benchmark complete. TXT written to: " + txtPath);

            if (makePlot) {
                System.out.println(" Generating plot...");
                PairwiseGraphGenerator.generateGraph(dynResults, bruteResults, pngPath);
            }
        }
    }

    /**
     * Overload of run() using defaults.
     */
    public static void run(String txtPath, String pngPath, boolean makePlot) throws Exception {
        run(txtPath, pngPath, makePlot, FIXED_LENGTH, MAX_PAIRS, STEP);
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
