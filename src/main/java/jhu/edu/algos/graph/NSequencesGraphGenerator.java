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
     * Includes actual values and fitted curves for both dynamic and brute force methods.
     *
     * @param dynResults List of dynamic programming LCS results.
     * @param bfResults  List of brute force LCS results (nullable).
     * @param outputPath PNG file path to save the generated chart.
     */
    public static void generateGraph(List<LCSResult> dynResults,
                                     List<LCSResult> bfResults,
                                     String outputPath) {

        // === Defensive null check ===
        if (dynResults == null || bfResults == null) {
            System.err.println("Error: Input result lists are null.");
            return;
        }

        // === Aggregate comparison counts by number of sequences ===
        Map<Integer, Long> dynSums = new TreeMap<>();
        Map<Integer, Long> bfSums = new TreeMap<>();

        int dynIndex = 0;
        int N = 4;  // Start from 4 sequences (choose 2 = 6 comparisons)

        while (dynIndex < dynResults.size()) {
            int comparisons = N * (N - 1) / 2;
            long dynTotal = 0;
            long bfTotal = 0;

            for (int i = 0; i < comparisons && dynIndex < dynResults.size(); i++, dynIndex++) {
                dynTotal += dynResults.get(dynIndex).getMetrics().getComparisonCount();
                if (bfResults.get(dynIndex) != null) {
                    bfTotal += bfResults.get(dynIndex).getMetrics().getComparisonCount();
                }
            }

            dynSums.put(N, dynTotal);
            if (bfTotal > 0) bfSums.put(N, bfTotal);

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

        // === Print fit models ===
        System.out.printf("%n==== Fitted Scaling Models ====%n");
        System.out.printf("Dynamic Programming: T(N) ≈ %.4f · N²%n", dynSlope);
        if (!bfX.isEmpty()) {
            System.out.printf("Brute Force        : T(N) ≈ %.4f · N²%n", bfSlope);
        } else {
            System.out.println("Brute Force        : No data available for fitting.");
        }
        System.out.println("================================\n");

        // === Create chart series ===
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
            baseline.add(x, 100.0 * x * x);  // Visual guide only
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

        // === Create chart ===
        JFreeChart chart = ChartFactory.createXYLineChart(
                "LCS Total Comparisons vs Number of Sequences (N)",
                "Number of Sequences (N)",
                "Total Character Comparisons",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        chart.addSubtitle(new TextTitle("Scaling Analysis of LCS Algorithms with Increasing N"));

        // === Configure aesthetics ===
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Colorblind-safe palette
        renderer.setSeriesPaint(0, new Color(0x1f78b4));  // Dyn actual
        renderer.setSeriesPaint(1, new Color(0xe31a1c));  // BF actual
        renderer.setSeriesPaint(2, new Color(0x999999));  // O(N²) theoretical
        renderer.setSeriesPaint(3, new Color(0x33a02c));  // Dyn fitted
        renderer.setSeriesPaint(4, new Color(0xff7f00));  // BF fitted

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, true);
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // === Annotate data points ===
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

        // === Export PNG ===
        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 1200, 800);
            System.out.println("Graph saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving graph: " + e.getMessage());
        }
    }
}
