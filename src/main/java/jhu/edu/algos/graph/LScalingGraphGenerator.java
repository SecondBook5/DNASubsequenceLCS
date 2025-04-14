package jhu.edu.algos.graph;

import jhu.edu.algos.lcs.LCSResult;
import jhu.edu.algos.utils.CurveFitter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.LogAxis;
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
import java.util.stream.Collectors;

/**
 * LScalingGraphGenerator:
 * Generates two side-by-side PNG plots showing LCS scaling behavior:
 * - One plot for Dynamic Programming (linear scale)
 * - One plot for Brute Force (logarithmic scale, L capped)
 * Plots both theoretical and fitted models.
 */
public class LScalingGraphGenerator {

    /**
     * Main method to generate both DP and BF plots from the benchmark results.
     *
     * @param dynamicResults    List of results from the DP algorithm.
     * @param bruteForceResults List of results from the BF algorithm (nulls included if skipped).
     * @param basePath          Output path ending in .png (e.g., "scaling_plot.png")
     */
    public static void generateGraph(List<LCSResult> dynamicResults,
                                     List<LCSResult> bruteForceResults,
                                     String basePath) {
        generateDynamicPlot(dynamicResults, basePath.replace(".png", "_dynamic.png"));
        generateBruteForcePlot(bruteForceResults, basePath.replace(".png", "_bruteforce.png"));
    }

    /**
     * Generates the Dynamic Programming scaling plot with linear Y-axis.
     *
     * @param results    All DP results from benchmark.
     * @param outputPath PNG file path to save the graph.
     */
    private static void generateDynamicPlot(List<LCSResult> results, String outputPath) {
        // Fit T(L) ≈ c * L^2 model
        double cDyn = CurveFitter.fitPowerLaw(results, 2.0);

        // Create series for actual data, theoretical curve, and fitted model
        XYSeries dynActual = new XYSeries("Observed: Dynamic Programming");
        XYSeries dynTheoretical = new XYSeries("Theoretical: O(L²)");
        XYSeries dynFitted = new XYSeries("Fitted: c·L²");

        for (LCSResult r : results) {
            int L = extractLengthFromLabel(r.getComparisonLabel());
            long comparisons = r.getMetrics().getComparisonCount();

            dynActual.add(L, comparisons);
            dynTheoretical.add(L, Math.pow(L, 2));
            dynFitted.add(L, cDyn * Math.pow(L, 2));
        }

        // Combine into dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dynActual);
        dataset.addSeries(dynTheoretical);
        dataset.addSeries(dynFitted);

        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "LCS Dynamic Programming Scaling",
                "Sequence Length (L)",
                "Total Comparisons",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Add subtitle for clarity
        chart.addSubtitle(new TextTitle("Growth of Dynamic LCS vs Theoretical and Fitted O(L²)"));

        // Customize plot
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        Color[] colors = { new Color(0x1f78b4), new Color(0x33a02c), new Color(0x6a3d9a) };

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, colors[i]);
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, true);
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Export as PNG
        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 1200, 800);
            System.out.println("Dynamic graph saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving dynamic graph: " + e.getMessage());
        }
    }

    /**
     * Generates the Brute Force scaling plot with logarithmic Y-axis.
     *
     * @param results    All BF results from benchmark (nulls skipped).
     * @param outputPath PNG file path to save the graph.
     */
    private static void generateBruteForcePlot(List<LCSResult> results, String outputPath) {
        List<LCSResult> filtered = results.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            System.err.println("No brute force data available.");
            return;
        }

        // Fit T(L) ≈ c * 2^L model
        double cBF = CurveFitter.fitExponential(filtered);

        // Create series
        XYSeries bfActual = new XYSeries("Observed: Brute Force");
        XYSeries bfTheoretical = new XYSeries("Theoretical: O(2^L)");
        XYSeries bfFitted = new XYSeries("Fitted: c·2^L");

        for (LCSResult r : filtered) {
            int L = extractLengthFromLabel(r.getComparisonLabel());
            long comparisons = r.getMetrics().getComparisonCount();

            bfActual.add(L, comparisons);
            bfTheoretical.add(L, Math.pow(2, L));
            bfFitted.add(L, cBF * Math.pow(2, L));
        }

        // Combine into dataset
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(bfActual);
        dataset.addSeries(bfTheoretical);
        dataset.addSeries(bfFitted);

        // Create the chart
        JFreeChart chart = ChartFactory.createXYLineChart(
                "LCS Brute Force Scaling",
                "Sequence Length (L)",
                "Total Comparisons (log scale)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        chart.addSubtitle(new TextTitle("Growth of Brute Force LCS vs O(2^L)"));

        // Customize log axis
        XYPlot plot = chart.getXYPlot();
        LogAxis logAxis = new LogAxis("Total Comparisons (log10)");
        logAxis.setBase(10);
        logAxis.setSmallestValue(1); // Avoid log(0)
        plot.setRangeAxis(logAxis);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        Color[] colors = { new Color(0xe31a1c), new Color(0xff7f00), new Color(0xb15928) };

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, colors[i]);
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, true);
        }

        // Apply renderer and theme
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Export as PNG
        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 1200, 800);
            System.out.println("Brute force graph saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving brute force graph: " + e.getMessage());
        }
    }

    /**
     * Extracts sequence length L from label like \"L30_P1\".
     * Returns -1 if label format is invalid.
     */
    private static int extractLengthFromLabel(String label) {
        try {
            int start = label.indexOf('L') + 1;
            int end = label.indexOf('_');
            if (start > 0 && end > start) {
                return Integer.parseInt(label.substring(start, end));
            }
        } catch (Exception ignored) {}
        return -1;
    }
}
