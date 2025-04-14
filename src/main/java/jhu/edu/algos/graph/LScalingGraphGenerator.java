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
 * Plots both theoretical and fitted models, and includes annotated formula overlays.
 */
public class LScalingGraphGenerator {

    /**
     * Main entry point to generate both plots: dynamic and brute force.
     * @param dynamicResults    List of dynamic programming LCS results
     * @param bruteForceResults List of brute force LCS results (nulls if skipped)
     * @param basePath          PNG output base path (e.g., "scaling_plot.png")
     */
    public static void generateGraph(List<LCSResult> dynamicResults,
                                     List<LCSResult> bruteForceResults,
                                     String basePath) {
        generateDynamicPlot(dynamicResults, basePath.replace(".png", "_dynamic.png"));
        generateBruteForcePlot(bruteForceResults, basePath.replace(".png", "_bruteforce.png"));
    }

    /**
     * Generates the Dynamic Programming plot with a linear y-axis.
     * Annotates with the fitted O(L^2) formula.
     */
    private static void generateDynamicPlot(List<LCSResult> results, String outputPath) {
        double cDyn = CurveFitter.fitPowerLaw(results, 2.0);

        XYSeries dynActual = new XYSeries("Observed: Dynamic Programming");
        XYSeries dynTheoretical = new XYSeries("Theoretical: O(L²)");
        XYSeries dynFitted = new XYSeries("Fitted: c·L²");

        int maxL = 0;
        for (LCSResult r : results) {
            int L = extractLengthFromLabel(r.getComparisonLabel());
            long comparisons = r.getMetrics().getComparisonCount();
            dynActual.add(L, comparisons);
            dynTheoretical.add(L, Math.pow(L, 2));
            dynFitted.add(L, cDyn * Math.pow(L, 2));
            maxL = Math.max(maxL, L);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dynActual);
        dataset.addSeries(dynTheoretical);
        dataset.addSeries(dynFitted);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "LCS Dynamic Programming Scaling",
                "Sequence Length (L)",
                "Total Comparisons",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        chart.addSubtitle(new TextTitle("Growth of Dynamic LCS vs Theoretical and Fitted O(L²)"));

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        Color[] colors = {
                new Color(0x1f78b4), // Observed
                new Color(0x33a02c), // Theoretical
                new Color(0x6a3d9a)  // Fitted
        };

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, colors[i]);
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, true);
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Annotate fitted equation
        String label = String.format("T(L) ≈ %.5f · L²", cDyn);
        XYTextAnnotation annotation = new XYTextAnnotation(label, maxL * 0.6, cDyn * Math.pow(maxL, 2) * 0.5);
        annotation.setFont(new Font("SansSerif", Font.BOLD, 12));
        annotation.setPaint(new Color(0x6a3d9a));
        annotation.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
        plot.addAnnotation(annotation);

        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 1200, 800);
            System.out.println("Dynamic graph saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving dynamic graph: " + e.getMessage());
        }
    }

    /**
     * Generates the Brute Force plot with a log-scale y-axis.
     * Annotates with the fitted O(2^L) formula.
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

        int maxL = 0;
        for (LCSResult r : filtered) {
            int L = extractLengthFromLabel(r.getComparisonLabel());
            long comparisons = r.getMetrics().getComparisonCount();
            bfActual.add(L, comparisons);
            bfTheoretical.add(L, Math.pow(2, L));
            bfFitted.add(L, cBF * Math.pow(2, L));
            maxL = Math.max(maxL, L);
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
        logAxis.setSmallestValue(1);
        plot.setRangeAxis(logAxis);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        Color[] colors = {
                new Color(0xe31a1c), // Observed
                new Color(0xff7f00), // Theoretical
                new Color(0xb15928)  // Fitted
        };

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

        // Annotate fitted equation
        String label = String.format("T(L) ≈ %.5f · 2^L", cBF);
        XYTextAnnotation annotation = new XYTextAnnotation(label, maxL * 0.6, cBF * Math.pow(2, maxL) * 0.5);
        annotation.setFont(new Font("SansSerif", Font.BOLD, 12));
        annotation.setPaint(new Color(0xb15928));
        annotation.setTextAnchor(TextAnchor.HALF_ASCENT_LEFT);
        plot.addAnnotation(annotation);

        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 1200, 800);
            System.out.println("Brute force graph saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving brute force graph: " + e.getMessage());
        }
    }

    /**
     * Utility to extract the sequence length L from label format "L30_P1"
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
