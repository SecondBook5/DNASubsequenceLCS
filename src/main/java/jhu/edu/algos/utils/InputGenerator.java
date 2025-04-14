package jhu.edu.algos.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * InputGenerator:
 * Generates benchmark-ready DNA sequences for LCS experiments.
 * Includes:
 *   - L-scaling inputs (variable sequence lengths)
 *   - N-scaling inputs (fixed-length, increasing sequence count)
 *   - Biologically meaningful test cases (palindromes, mutations, repeats, etc.)
 */
public class InputGenerator {

    // L-scaling sequence lengths (each bucket is L to L+9)
    private static final int[] SCALING_LENGTHS = {10, 20, 30, 40, 50, 60};

    // N-scaling number of sequence pairs (fixed length, increasing N)
    private static final int[] PAIR_COUNTS = {5, 10, 20, 30, 40};

    // Random seed for reproducibility
    private static final Random rand = new Random(42);

    // DNA alphabet
    public static final char[] DNA_BASES = {'A', 'C', 'G', 'T'};

    // Output directory
    private static final String OUTPUT_DIR = "input";

    public static void main(String[] args) throws IOException {
        generateLScalingInputs();
        generateNScalingInputs();
        generateBiologicalEdgeCases();
        System.out.println("All input files written to /input");
    }

    /**
     * Generates synthetic sequence pairs for L-scaling benchmarks.
     * Simulates bucket variability: each L represents [L, L+9] true length.
     */
    private static void generateLScalingInputs() throws IOException {
        for (int baseLen : SCALING_LENGTHS) {
            List<String> lines = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                int len1 = baseLen + rand.nextInt(10);
                int len2 = baseLen + rand.nextInt(10);
                lines.add("S" + (2 * i - 1) + " = " + randomDNA(len1));
                lines.add("S" + (2 * i) + " = " + randomDNA(len2));
            }
            writeToFile("Lscaling_length" + baseLen + "_pairs5.txt", lines);
        }
    }

    /**
     * Generates synthetic sequence pairs for N-scaling benchmarks.
     * Uses fixed length 30 for all sequences.
     */
    private static void generateNScalingInputs() throws IOException {
        int fixedLength = 30;
        for (int pairs : PAIR_COUNTS) {
            List<String> lines = new ArrayList<>();
            for (int i = 1; i <= pairs; i++) {
                lines.add("S" + (2 * i - 1) + " = " + randomDNA(fixedLength));
                lines.add("S" + (2 * i) + " = " + randomDNA(fixedLength));
            }
            writeToFile("Nscaling_length" + fixedLength + "_pairs" + pairs + ".txt", lines);
        }
    }

    /**
     * Generates a diverse set of biological/algorithmic edge cases for LCS benchmarking.
     * Each case class is randomly constructed to simulate real-world patterns.
     */
    private static void generateBiologicalEdgeCases() throws IOException {
        List<String> lines = new ArrayList<>();
        int idx = 1;

        // 1. Identical Sequences (max LCS)
        for (int i = 0; i < 2; i++) {
            String seq = randomDNA(20);
            lines.add("S" + (idx++) + " = " + seq);
            lines.add("S" + (idx++) + " = " + seq);
        }

        // 2. Completely Different (min LCS = 0)
        for (int i = 0; i < 2; i++) {
            lines.add("S" + (idx++) + " = " + repeatChar('A', 20));
            lines.add("S" + (idx++) + " = " + repeatChar('T', 20));
        }

        // 3. GC-rich vs AT-rich (compositional bias)
        for (int i = 0; i < 2; i++) {
            lines.add("S" + (idx++) + " = " + biasedDNA(20, 0.9));  // GC-rich
            lines.add("S" + (idx++) + " = " + biasedDNA(20, 0.1));  // AT-rich
        }

        // 4. Palindromes (common in regulatory DNA)
        for (int i = 0; i < 2; i++) {
            String half = randomDNA(10);
            String full = half + new StringBuilder(half).reverse();
            lines.add("S" + (idx++) + " = " + full);
            lines.add("S" + (idx++) + " = " + full);
        }

        // 5. Reverse Complements (double-stranded DNA)
        for (int i = 0; i < 2; i++) {
            String fwd = randomDNA(15);
            lines.add("S" + (idx++) + " = " + fwd);
            lines.add("S" + (idx++) + " = " + reverseComplement(fwd));
        }

        // 6. Prefix-Suffix Overlaps (contig stitching / recombination)
        for (int i = 0; i < 2; i++) {
            String overlap = randomDNA(8);
            String s1 = overlap + randomDNA(12);
            String s2 = randomDNA(12) + overlap;
            lines.add("S" + (idx++) + " = " + s1);
            lines.add("S" + (idx++) + " = " + s2);
        }

        // 7. Tandem Repeats (STRs, satellite DNA)
        for (int i = 0; i < 2; i++) {
            String repeat = "AT";
            String repString = repeat.repeat(10);
            lines.add("S" + (idx++) + " = " + repString);
            lines.add("S" + (idx++) + " = " + repString);
        }

        // 8. Point Mutations (SNPs, small edits)
        for (int i = 0; i < 2; i++) {
            String base = randomDNA(20);
            char[] mut = base.toCharArray();
            int pos = rand.nextInt(20);
            char old = mut[pos];
            do {
                mut[pos] = DNA_BASES[rand.nextInt(4)];
            } while (mut[pos] == old);
            lines.add("S" + (idx++) + " = " + base);
            lines.add("S" + (idx++) + " = " + new String(mut));
        }

        // 9. Indels (insertions/deletions)
        for (int i = 0; i < 2; i++) {
            String base = randomDNA(20);
            int del = rand.nextInt(5) + 1;
            lines.add("S" + (idx++) + " = " + base);
            lines.add("S" + (idx++) + " = " + base.substring(0, base.length() - del));
        }

        writeToFile("biological_cases.txt", lines);
    }

    /**
     * Writes a list of formatted sequence lines to the output directory.
     */
    private static void writeToFile(String filename, List<String> lines) throws IOException {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, filename);
        try (FileWriter fw = new FileWriter(file)) {
            for (String line : lines) fw.write(line + System.lineSeparator());
        }
    }

    /**
     * Generates a random DNA string.
     */
    private static String randomDNA(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(DNA_BASES[rand.nextInt(4)]);
        return sb.toString();
    }

    /**
     * Repeats a character to form a string.
     */
    private static String repeatChar(char c, int len) {
        return String.valueOf(c).repeat(len);
    }

    /**
     * Returns a GC-biased DNA sequence.
     *
     * @param len     total sequence length
     * @param gcBias  probability between 0.0 and 1.0 that a base is G/C
     */
    private static String biasedDNA(int len, double gcBias) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            if (rand.nextDouble() < gcBias)
                sb.append(rand.nextBoolean() ? 'G' : 'C');
            else
                sb.append(rand.nextBoolean() ? 'A' : 'T');
        }
        return sb.toString();
    }

    /**
     * Computes the reverse complement of a DNA sequence.
     */
    private static String reverseComplement(String seq) {
        StringBuilder sb = new StringBuilder(seq.length());
        for (int i = seq.length() - 1; i >= 0; i--) {
            char base = seq.charAt(i);
            sb.append(switch (base) {
                case 'A' -> 'T';
                case 'T' -> 'A';
                case 'C' -> 'G';
                case 'G' -> 'C';
                default -> base;
            });
        }
        return sb.toString();
    }
}
