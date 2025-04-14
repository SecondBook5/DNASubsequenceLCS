package jhu.edu.algos;

import jhu.edu.algos.benchmark.LScalingBenchmark;
import jhu.edu.algos.benchmark.NSequencesBenchmark;

/**
 * Main.java is the command-line entry point for the LCS project.
 * Supports:
 *   - Comparison mode (default): <input_file.txt> <output_file.txt> [--matrix]
 *   - Length benchmark: benchmark-length <output.txt> [--plot]
 *   - N benchmark:      benchmark-n <output.txt> [--plot]
 */
public class Main {

    /**
     * CLI dispatcher that forwards to the appropriate LCS module.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printUsageAndExit();
            }

            // === Benchmark Modes (require mode flag) ===
            if (args[0].equalsIgnoreCase("benchmark-length")) {
                if (args.length < 2 || args.length > 3) {
                    System.err.println("Usage: java Main benchmark-length <output.txt> [--plot]");
                    System.exit(1);
                }

                String txtFile = args[1];
                boolean plot = args.length == 3 && args[2].equalsIgnoreCase("--plot");
                if (!txtFile.endsWith(".txt")) {
                    System.err.println("Error: Benchmark output file must be a .txt file.");
                    System.exit(1);
                }

                String pngFile = txtFile.replaceAll("\\.txt$", "_length_plot.png");
                LScalingBenchmark.run(txtFile, pngFile, plot);
                return;
            }

            if (args[0].equalsIgnoreCase("benchmark-n")) {
                if (args.length < 2 || args.length > 3) {
                    System.err.println("Usage: java Main benchmark-n <output.txt> [--plot]");
                    System.exit(1);
                }

                String txtFile = args[1];
                boolean plot = args.length == 3 && args[2].equalsIgnoreCase("--plot");
                if (!txtFile.endsWith(".txt")) {
                    System.err.println("Error: Benchmark output file must be a .txt file.");
                    System.exit(1);
                }

                String pngFile = txtFile.replaceAll("\\.txt$", "_n_plot.png");
                NSequencesBenchmark.run(txtFile, pngFile, plot);
                return;
            }

            // === Default Mode: Compare ===
            if (args.length < 2 || args.length > 3) {
                System.err.println("Usage: java Main <input_file.txt> <output_file.txt> [--matrix]");
                System.exit(1);
            }

            String inputFile = args[0];
            String outputFile = args[1];
            boolean showMatrix = args.length == 3 && args[2].equalsIgnoreCase("--matrix");

            if (!inputFile.endsWith(".txt") || !outputFile.endsWith(".txt")) {
                System.err.println("Error: Both input and output files must end with .txt");
                System.exit(1);
            }

            LCSDriver.runFromFile(inputFile, outputFile, showMatrix);

        } catch (Exception e) {
            System.err.println(" Fatal error: " + e.getClass().getSimpleName() + " - " + e.getMessage());

            // Helpful diagnostics
            if (e instanceof IllegalArgumentException) {
                System.err.println(" Tip: Check that your input file format and arguments are correct.");
            } else if (e instanceof java.io.FileNotFoundException) {
                System.err.println(" Tip: Input file not found. Check the filename or path.");
            } else if (e instanceof java.io.IOException) {
                System.err.println(" Tip: Check if the output file is writable or if disk space is sufficient.");
            }

            // Print summarized stack trace
            StackTraceElement[] trace = e.getStackTrace();
            if (trace.length > 0) System.err.println(" Location: " + trace[0]);
            if (trace.length > 1) System.err.println("            " + trace[1]);
            if (trace.length > 2) System.err.println("            " + trace[2]);

            System.exit(1);
        }
    }

    /**
     * Prints help instructions and exits.
     */
    private static void printUsageAndExit() {
        System.err.println("Usage:");
        System.err.println("  java Main <input_file.txt> <output_file.txt> [--matrix]");
        System.err.println("  java Main benchmark-length <output_file.txt> [--plot]");
        System.err.println("  java Main benchmark-n <output_file.txt> [--plot]");
        System.exit(1);
    }
}
