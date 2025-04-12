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
     * Includes actual values and fitted lines for both dynamic and brute force methods.
     *
     * @param dynResults   List of dynamic programming LCS results.
     * @param bfResults    List of brute force LCS results (nullable).
     * @param outputPath   PNG file path to save the generated chart.
     */
    public static void generateGraph(List<LCSResult> dynResults,
                                     List<LCSResult> bfResults,
                                     String outputPath) {

        // === Defensive Null Check ===
        if (dynResults == null || bfResults == null) {
            System.err.println("Error: Input result lists are null.");
            return;
        }

        // === Group into "number of sequences" buckets ===
        // Each group contains comparisons for n sequences → C(n, 2) comparisons
        Map<Integer, Long> dynSums = new TreeMap<>();
        Map<Integer, Long> bfSums = new TreeMap<>();
        int currentIndex = 0;
        int groupSize = 2;

        while (currentIndex < dynResults.size()) {
            int comparisonsInGroup = (groupSize * (groupSize - 1)) / 2;
            long dynTotal = 0;
            long bfTotal = 0;

            for (int i = 0; i < comparisonsInGroup && currentIndex < dynResults.size(); i++, currentIndex++) {
                dynTotal += dynResults.get(currentIndex).getMetrics().getComparisonCount();
                if (bfResults.get(currentIndex) != null) {
                    bfTotal += bfResults.get(currentIndex).getMetrics().getComparisonCount();
                }
            }

            dynSums.put(groupSize, dynTotal);
            if (bfTotal > 0) bfSums.put(groupSize, bfTotal);

            groupSize++;
        }

        // === Fit T(n) ≈ c·n² using linear regression on n² vs total comparisons ===
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

        // === Print fit models to terminal ===
        System.out.printf("%n==== Fitted Models ====%n");
        System.out.printf("Dynamic Programming: T(n) ≈ %.4f · n²%n", dynSlope);
        if (!bfX.isEmpty()) {
            System.out.printf("Brute Force        : T(n) ≈ %.4f · n²%n", bfSlope);
        }
        System.out.println("=======================\n");

        // === Prepare plotting datasets ===
        XYSeries dynActual = new XYSeries("Actual: Dynamic");
        XYSeries bfActual = new XYSeries("Actual: Brute Force");
        XYSeries dynFitted = new XYSeries("Fitted: c·n² (Dyn)");
        XYSeries bfFitted = new XYSeries("Fitted: c·n² (BF)");
        XYSeries baseline = new XYSeries("Expected: O(n²)");

        for (Integer n : dynSums.keySet()) {
            double x = n;
            double y = dynSums.get(n);
            dynActual.add(x, y);
            dynFitted.add(x, dynSlope * x * x);
            baseline.add(x, 100.0 * x * x); // baseline arbitrary c=100
        }

        for (Integer n : bfSums.keySet()) {
            double x = n;
            double y = bfSums.get(n);
            bfActual.add(x, y);
            bfFitted.add(x, bfSlope * x * x);
        }

        // === Combine series into dataset ===
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dynActual);
        dataset.addSeries(bfActual);
        dataset.addSeries(baseline);
        dataset.addSeries(dynFitted);
        dataset.addSeries(bfFitted);

        // === Configure chart ===
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Comparison Scaling vs Number of Sequences",
                "Number of Sequences",
                "Total Comparisons",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        renderer.setSeriesPaint(0, Color.BLUE); // Actual: Dynamic
        renderer.setSeriesPaint(1, Color.RED); // Actual: Brute Force
        renderer.setSeriesPaint(2, Color.LIGHT_GRAY); // Expected: O(n²)
        renderer.setSeriesPaint(3, Color.CYAN); // Fitted: c·n² (Dyn)
        renderer.setSeriesPaint(4, Color.MAGENTA); // Fitted: c·n² (BF)

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesLinesVisible(i, true);
        }

        // Set plot theme
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Add annotations
        dynSums.forEach((n, val) -> {
            XYTextAnnotation note = new XYTextAnnotation("Dyn: " + val, n, val);
            note.setFont(new Font("SansSerif", Font.PLAIN, 9));
            note.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
            plot.addAnnotation(note);
        });

        bfSums.forEach((n, val) -> {
            XYTextAnnotation note = new XYTextAnnotation("BF: " + val, n, val);
            note.setFont(new Font("SansSerif", Font.PLAIN, 9));
            note.setTextAnchor(TextAnchor.TOP_LEFT);
            plot.addAnnotation(note);
        });

        // Save the chart to PNG
        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 1000, 650);
            System.out.println("Graph saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving graph: " + e.getMessage());
        }
    }
}
