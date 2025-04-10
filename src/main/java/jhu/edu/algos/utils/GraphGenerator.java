package jhu.edu.algos.utils;

import jhu.edu.algos.lcs.LCSResult;
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
import java.util.stream.Collectors;

/**
 * GraphGenerator creates a line chart visualizing the scaling behavior
 * of the LCS problem under dynamic programming vs. brute force.
 */
public class GraphGenerator {

    /**
     * Generates a comparative plot of dynamic and brute force LCS comparison counts.
     * Adds annotations, theoretical baselines, and fitted model overlays.
     *
     * @param dynamicResults    LCS results using the dynamic programming algorithm.
     * @param bruteForceResults LCS results using the brute-force algorithm.
     * @param outputPath        File path to save the PNG output graph.
     */
    public static void generateGraph(List<LCSResult> dynamicResults,
                                     List<LCSResult> bruteForceResults,
                                     String outputPath) {

        if (dynamicResults == null || bruteForceResults == null) {
            System.err.println("Error: One of the result lists is null.");
            return;
        }

        // === Map brute-force results by label (safely skip nulls) ===
        Map<String, LCSResult> bruteMap = new HashMap<>();
        for (LCSResult result : bruteForceResults) {
            if (result != null) {
                bruteMap.put(result.getComparisonLabel(), result);
            }
        }

        // === Filter out nulls before curve fitting ===
        List<LCSResult> cleanedBrute = bruteForceResults.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // === Fit theoretical models to actual data ===
        double cDynamic = CurveFitter.fitPowerLaw(dynamicResults, 2.0);
        double cBrute = cleanedBrute.isEmpty() ? 0.0 : CurveFitter.fitExponential(cleanedBrute);

        System.out.printf("Fitted constant for Dynamic (T ≈ c·n²): %.5f%n", cDynamic);
        System.out.printf("Fitted constant for Brute Force (T ≈ c·2ⁿ): %.5f%n", cBrute);

        // === Series containers ===
        XYSeries dynActual = new XYSeries("Actual: Dynamic Programming");
        XYSeries bfActual = new XYSeries("Actual: Brute Force");
        XYSeries expectedN2 = new XYSeries("Expected: O(n²)");
        XYSeries expected2N = new XYSeries("Expected: O(2ⁿ)");
        XYSeries fittedN2 = new XYSeries("Fitted: c·n²");
        XYSeries fitted2N = new XYSeries("Fitted: c·2ⁿ");

        // === Populate data ===
        for (LCSResult dyn : dynamicResults) {
            String label = dyn.getComparisonLabel();
            LCSResult bf = bruteMap.get(label);  // May be null

            double avgN = (dyn.getFirstInput().length() + dyn.getSecondInput().length()) / 2.0;
            long dynComp = dyn.getMetrics().getComparisonCount();

            dynActual.add(avgN, dynComp);
            expectedN2.add(avgN, Math.pow(avgN, 2));
            fittedN2.add(avgN, cDynamic * Math.pow(avgN, 2));

            if (bf != null) {
                long bfComp = bf.getMetrics().getComparisonCount();
                bfActual.add(avgN, bfComp);
                expected2N.add(avgN, Math.pow(2, avgN));
                fitted2N.add(avgN, cBrute * Math.pow(2, avgN));
            }
        }

        // === Build dataset ===
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dynActual);
        dataset.addSeries(bfActual);
        dataset.addSeries(expectedN2);
        dataset.addSeries(expected2N);
        dataset.addSeries(fittedN2);
        dataset.addSeries(fitted2N);

        // === Create chart ===
        JFreeChart chart = ChartFactory.createXYLineChart(
                "LCS Comparison Count vs Input Size",
                "Average Input Size (n)",
                "Character Comparisons",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // === Customize rendering ===
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        renderer.setSeriesPaint(0, Color.BLUE);     // Actual Dynamic
        renderer.setSeriesPaint(1, Color.RED);      // Actual Brute Force
        renderer.setSeriesPaint(2, Color.GREEN);    // Expected O(n²)
        renderer.setSeriesPaint(3, Color.ORANGE);   // Expected O(2ⁿ)
        renderer.setSeriesPaint(4, Color.CYAN);     // Fitted c·n²
        renderer.setSeriesPaint(5, Color.MAGENTA);  // Fitted c·2ⁿ

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesLinesVisible(i, true);
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // === Annotate actual values ===
        for (LCSResult dyn : dynamicResults) {
            String label = dyn.getComparisonLabel();
            LCSResult bf = bruteMap.get(label);

            double avgN = (dyn.getFirstInput().length() + dyn.getSecondInput().length()) / 2.0;
            long dynComp = dyn.getMetrics().getComparisonCount();

            // Dynamic annotation
            XYTextAnnotation dynNote = new XYTextAnnotation("Dyn: " + dynComp, avgN, dynComp);
            dynNote.setPaint(Color.BLUE);
            dynNote.setFont(new Font("SansSerif", Font.PLAIN, 10));
            dynNote.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
            plot.addAnnotation(dynNote);

            // Brute force annotation (only if data exists)
            if (bf != null) {
                long bfComp = bf.getMetrics().getComparisonCount();
                XYTextAnnotation bfNote = new XYTextAnnotation("BF: " + bfComp, avgN, bfComp);
                bfNote.setPaint(Color.RED);
                bfNote.setFont(new Font("SansSerif", Font.PLAIN, 10));
                bfNote.setTextAnchor(TextAnchor.BOTTOM_LEFT);
                plot.addAnnotation(bfNote);
            }
        }

        // === Save as PNG ===
        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 1000, 650);
            System.out.println("Graph saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving graph: " + e.getMessage());
        }
    }
}
