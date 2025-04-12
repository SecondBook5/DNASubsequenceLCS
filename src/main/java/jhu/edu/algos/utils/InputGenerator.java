package jhu.edu.algos.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * InputGenerator:
 * Generates test inputs for scaling benchmarks and algorithm stress testing.
 * Inputs are saved under the "input/" directory using S1 = ..., S2 = ... format.
 */
public class InputGenerator {

    private static final int[] SCALING_LENGTHS = {10, 20, 30, 40, 50, 60};
    private static final int[] PAIR_COUNTS = {5, 10, 20, 30, 40};
    private static final Random rand = new Random(42);
    private static final char[] DNA_BASES = {'A', 'C', 'G', 'T'};
    private static final String OUTPUT_DIR = "input";

    public static void main(String[] args) throws IOException {
        generateScalingLengthInputs();
        generateScalingPairsInputs();
        System.out.println("Input generation complete. Files written to: /input");
    }

    /**
     * Generates files for scaling **sequence length** with fixed pair count.
     */
    private static void generateScalingLengthInputs() throws IOException {
        for (int len : SCALING_LENGTHS) {
            List<String> lines = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                String s1 = randomDNA(len);
                String s2 = randomDNA(len);
                lines.add("S" + (2 * i - 1) + " = " + s1);
                lines.add("S" + (2 * i) + " = " + s2);
            }
            writeToFile("length" + len + "_pairs5.txt", lines);
        }
    }

    /**
     * Generates files for scaling **number of pairs** with fixed sequence length.
     */
    private static void generateScalingPairsInputs() throws IOException {
        int fixedLength = 30;
        for (int pairs : PAIR_COUNTS) {
            List<String> lines = new ArrayList<>();
            for (int i = 1; i <= pairs; i++) {
                String s1 = randomDNA(fixedLength);
                String s2 = randomDNA(fixedLength);
                lines.add("S" + (2 * i - 1) + " = " + s1);
                lines.add("S" + (2 * i) + " = " + s2);
            }
            writeToFile("length" + fixedLength + "_pairs" + pairs + ".txt", lines);
        }
    }

    /**
     * Writes sequence pairs to a file in the correct format.
     */
    private static void writeToFile(String filename, List<String> lines) throws IOException {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, filename);
        try (FileWriter fw = new FileWriter(file)) {
            for (String line : lines) {
                fw.write(line + System.lineSeparator());
            }
        }
    }

    /**
     * Generates a random DNA string of given length.
     */
    private static String randomDNA(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(DNA_BASES[rand.nextInt(4)]);
        }
        return sb.toString();
    }
}
