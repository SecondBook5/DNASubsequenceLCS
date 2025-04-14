package jhu.edu.algos.graph;

import jhu.edu.algos.lcs.LCSResult;
import jhu.edu.algos.utils.CurveFitter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * NSequencesGraphGenerator:
 * Visualizes how total comparisons scale as the number of sequences increases.
 * Assumes a fixed sequence length while varying the number of input sequences.
 * For n sequences, number of comparisons is n choose 2.
 */
public class NSequencesGraphGenerator {

    /**
     * Generates a benchmark plot showing total comparisons vs number of sequences.
     * Includes actual values, fitted models, and baseline complexity curves.
     *
     * @param dynResults List of LCSResult from dynamic programming algorithm
     * @param bfResults  List of LCSResult from brute force algorithm (nullable/skipped)
     * @param outputPath Path to save PNG plot
     */
    public static void generateGraph(List<LCSResult> dynResults,
                                     List<LCSResult> bfResults,
                                     String outputPath) {

        // === Defensive null check ===
        if (dynResults == null || bfResults == null) {
            System.err.println("Error: Input result lists are null.");
            return;
        }

        // === Aggregate total comparisons for each N ===
        Map<Integer, Long> dynSums = new TreeMap<>();
        Map<Integer, Long> bfSums = new TreeMap<>();
        Map<Integer, Long> dynTime = new TreeMap<>();
        Map<Integer, Long> bfTime = new TreeMap<>();

        int dynIndex = 0;
        int N = 4;

        while (dynIndex < dynResults.size()) {
            int comparisons = N * (N - 1) / 2;
            long dynTotal = 0, bfTotal = 0;
            long dynTotalTime = 0, bfTotalTime = 0;

            for (int i = 0; i < comparisons && dynIndex < dynResults.size(); i++, dynIndex++) {
                LCSResult dyn = dynResults.get(dynIndex);
                dynTotal += dyn.getMetrics().getComparisonCount();
                dynTotalTime += dyn.getMetrics().getElapsedTimeMs();

                LCSResult bf = bfResults.get(dynIndex);
                if (bf != null) {
                    bfTotal += bf.getMetrics().getComparisonCount();
                    bfTotalTime += bf.getMetrics().getElapsedTimeMs();
                }
            }

            dynSums.put(N, dynTotal);
            dynTime.put(N, dynTotalTime);
            if (bfTotal > 0) {
                bfSums.put(N, bfTotal);
                bfTime.put(N, bfTotalTime);
            }

            N++;
        }

        // === Fit T(N) ≈ c·N² using linear regression ===
        List<Double> dynX = new ArrayList<>();
        List<Double> dynY = new ArrayList<>();
        for (Integer n : dynSums.keySet()) {
            dynX.add(Math.pow(n, 2));
            dynY.add(dynSums.get(n).doubleValue());
        }
        double dynSlope = CurveFitter.fitLinear(dynX, dynY);

        List<Double> bfX = new ArrayList<>();
        List<Double> bfY = new ArrayList<>();
        for (Integer n : bfSums.keySet()) {
            bfX.add(Math.pow(n, 2));
            bfY.add(bfSums.get(n).doubleValue());
        }
        double bfSlope = CurveFitter.fitLinear(bfX, bfY);

        // === Print fit model equations to console ===
        System.out.printf("%n==== Fitted Scaling Models ====%n");
        System.out.printf("Dynamic Programming: T(N) ≈ %.4f · N²%n", dynSlope);
        if (!bfX.isEmpty()) {
            System.out.printf("Brute Force        : T(N) ≈ %.4f · N²%n", bfSlope);
        } else {
            System.out.println("Brute Force        : No data available for fitting.");
        }
        System.out.println("================================\n");

        // === Create plot series ===
        XYSeries dynActual = new XYSeries("Observed: Dynamic Programming");
        XYSeries bfActual = new XYSeries("Observed: Brute Force");
        XYSeries dynFitted = new XYSeries("Fitted: c·N² (Dyn)");
        XYSeries bfFitted = new XYSeries("Fitted: c·N² (BF)");
        XYSeries baseline = new XYSeries("Theoretical: O(N²)");

        for (Integer n : dynSums.keySet()) {
            double x = n;
            double y = dynSums.get(n);
            dynActual.add(x, y);
            dynFitted.add(x, dynSlope * x * x);
            baseline.add(x, 100.0 * x * x); // baseline visual
        }

        for (Integer n : bfSums.keySet()) {
            double x = n;
            double y = bfSums.get(n);
            bfActual.add(x, y);
            bfFitted.add(x, bfSlope * x * x);
        }

        // === Assemble dataset ===
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dynActual);
        dataset.addSeries(bfActual);
        dataset.addSeries(baseline);
        dataset.addSeries(dynFitted);
        dataset.addSeries(bfFitted);

        // === Build chart ===
        JFreeChart chart = ChartFactory.createXYLineChart(
                "LCS Total Comparisons vs Number of Sequences (N)",
                "Number of Sequences (N)",
                "Total Character Comparisons",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        chart.addSubtitle(new TextTitle("Scaling Analysis of LCS Algorithms with Increasing N"));

        // === Customize aesthetics ===
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        Color[] palette = {
                new Color(0x1f78b4), // DP actual
                new Color(0xe31a1c), // BF actual
                new Color(0x999999), // O(N²)
                new Color(0x33a02c), // DP fitted
                new Color(0xff7f00)  // BF fitted
        };

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, palette[i]);
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, true);
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // === Add annotations for each N point ===
        dynSums.forEach((n, val) -> {
            XYTextAnnotation note = new XYTextAnnotation("Dyn: " + val, n, val);
            note.setFont(new Font("SansSerif", Font.PLAIN, 9));
            note.setPaint(new Color(0x1f78b4));
            note.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
            plot.addAnnotation(note);
        });

        bfSums.forEach((n, val) -> {
            XYTextAnnotation note = new XYTextAnnotation("BF: " + val, n, val);
            note.setFont(new Font("SansSerif", Font.PLAIN, 9));
            note.setPaint(new Color(0xe31a1c));
            note.setTextAnchor(TextAnchor.TOP_LEFT);
            plot.addAnnotation(note);
        });

        // === Add fitted curve annotations (slopes) ===
        if (!dynX.isEmpty()) {
            XYTextAnnotation dynFitNote = new XYTextAnnotation(
                    String.format("T(N) ≈ %.2f·N²", dynSlope),
                    Collections.max(dynSums.keySet()) * 0.6,
                    Collections.max(dynSums.values()) * 1.2
            );
            dynFitNote.setFont(new Font("SansSerif", Font.BOLD, 10));
            dynFitNote.setPaint(new Color(0x33a02c));
            dynFitNote.setTextAnchor(TextAnchor.BASELINE_LEFT);
            plot.addAnnotation(dynFitNote);
        }

        if (!bfX.isEmpty()) {
            XYTextAnnotation bfFitNote = new XYTextAnnotation(
                    String.format("T(N) ≈ %.2f·N²", bfSlope),
                    Collections.max(bfSums.keySet()) * 0.6,
                    Collections.max(bfSums.values()) * 1.05
            );
            bfFitNote.setFont(new Font("SansSerif", Font.BOLD, 10));
            bfFitNote.setPaint(new Color(0xff7f00));
            bfFitNote.setTextAnchor(TextAnchor.BASELINE_LEFT);
            plot.addAnnotation(bfFitNote);
        }

        // === Export final PNG ===
        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 1200, 800);
            System.out.println("Graph saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving graph: " + e.getMessage());
        }
    }
}
