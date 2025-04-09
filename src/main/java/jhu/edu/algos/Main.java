package jhu.edu.algos;

/**
 * Main.java serves as the command-line entry point and dispatcher for
 * the DNASubsequenceLCS project. Supports both comparison and scaling modes.
 *
 * Usage:
 *   java Main compare <input_file.txt> <output_file.txt>
 *   java Main benchmark <output_file.csv> [--plot]
 */
public class Main {

    /**
     * CLI dispatcher that forwards to the appropriate LCS module.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsageAndExit();
        }

        String mode = args[0].toLowerCase();

        try {
            switch (mode) {
                case "compare":
                    // Compare mode requires input and output .txt files
                    if (args.length != 3) {
                        System.err.println("Usage: java Main compare <input_file.txt> <output_file.txt>");
                        System.exit(1);
                    }

                    String inputFile = args[1];
                    String outputFile = args[2];

                    // Validate extensions
                    if (!inputFile.endsWith(".txt") || !outputFile.endsWith(".txt")) {
                        System.err.println("Error: Both input and output files must end with .txt");
                        System.exit(1);
                    }

                    // Dispatch to comparison driver
                    LCSDriver.runFromFile(inputFile, outputFile);
                    break;

                case "benchmark":
                    // Benchmark mode requires a CSV output file and optional --plot flag
                    if (args.length < 2 || args.length > 3) {
                        System.err.println("Usage: java Main benchmark <output_file.csv> [--plot]");
                        System.exit(1);
                    }

                    String csvFile = args[1];
                    boolean plot = args.length == 3 && args[2].equalsIgnoreCase("--plot");

                    if (!csvFile.endsWith(".csv")) {
                        System.err.println("Error: Benchmark output file must be a .csv file.");
                        System.exit(1);
                    }

                    // Derive PNG output file from CSV if plot flag is used
                    String pngFile = csvFile.replaceAll("\\.csv$", "_plot.png");

                    // Dispatch to benchmark module
                    ScalingBenchmark.run(csvFile, pngFile, plot);
                    break;

                default:
                    System.err.println("Unrecognized command: " + mode);
                    printUsageAndExit();
            }

        } catch (Exception e) {
            System.err.println(" Fatal error: " + e.getClass().getSimpleName() + " - " + e.getMessage());

            // Suggest remedies for known error types
            if (e instanceof IllegalArgumentException) {
                System.err.println(" Tip: Check that your input file format and arguments are correct.");
            } else if (e instanceof java.io.FileNotFoundException) {
                System.err.println(" Tip: Input file not found. Check the filename or path.");
            } else if (e instanceof java.io.IOException) {
                System.err.println(" Tip: Check if the output file is writable or if disk space is sufficient.");
            }

            // Print summarized stack trace
            StackTraceElement[] trace = e.getStackTrace();
            if (trace.length > 0) {
                System.err.println(" Location: " + trace[0]);
            }
            if (trace.length > 1) {
                System.err.println("            " + trace[1]);
            }
            if (trace.length > 2) {
                System.err.println("            " + trace[2]);
            }

            System.exit(1);
        }
    }

    /**
     * Prints help and exits.
     */
    private static void printUsageAndExit() {
        System.err.println("Usage:");
        System.err.println("  java Main compare <input_file.txt> <output_file.txt>");
        System.err.println("  java Main benchmark <output_file.csv> [--plot]");
        System.exit(1);
    }
}
