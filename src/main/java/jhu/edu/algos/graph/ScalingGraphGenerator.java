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
import java.util.stream.Collectors;

/**
 * GraphGenerator creates a line chart visualizing the scaling behavior
 * of the LCS problem under dynamic programming vs. brute force.
 * It includes:
 * - Actual comparison counts
 * - Theoretical O(n²) and O(2ⁿ) curves
 * - Fitted models and annotations
 * - Prints the explicit fitted curve formulas
 */
public class ScalingGraphGenerator {

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

        // === Map brute force results by label ===
        Map<String, LCSResult> bruteMap = bruteForceResults.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(LCSResult::getComparisonLabel, r -> r));

        // === Filter out nulls for fitting ===
        List<LCSResult> cleanedBrute = bruteForceResults.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // === Fit scaling models ===
        double cDynamic = CurveFitter.fitPowerLaw(dynamicResults, 2.0);
        double cBrute = cleanedBrute.isEmpty() ? 0.0 : CurveFitter.fitExponential(cleanedBrute);

        // === Print scaling models to terminal ===
        System.out.printf("%n==== Fitted Scaling Models ====%n");
        System.out.printf("Dynamic Programming: T(n) ≈ %.5f · n²%n", cDynamic);
        if (!cleanedBrute.isEmpty()) {
            System.out.printf("Brute Force        : T(n) ≈ %.5f · 2ⁿ%n", cBrute);
        } else {
            System.out.println("Brute Force        : No data available for fitting.");
        }
        System.out.println("================================\n");

        // === Prepare chart series ===
        XYSeries dynActual = new XYSeries("Actual: Dynamic Programming");
        XYSeries bfActual = new XYSeries("Actual: Brute Force");
        XYSeries expectedN2 = new XYSeries("Expected: O(n²)");
        XYSeries expected2N = new XYSeries("Expected: O(2ⁿ)");
        XYSeries fittedN2 = new XYSeries("Fitted: c·n²");
        XYSeries fitted2N = new XYSeries("Fitted: c·2ⁿ");

        for (LCSResult dyn : dynamicResults) {
            String label = dyn.getComparisonLabel();
            LCSResult bf = bruteMap.get(label);

            // === Extract L from label pattern like "L30_P1" ===
            int L = extractLengthFromLabel(label);

            long dynComp = dyn.getMetrics().getComparisonCount();
            dynActual.add(L, dynComp);
            expectedN2.add(L, Math.pow(L, 2));
            fittedN2.add(L, cDynamic * Math.pow(L, 2));

            if (bf != null) {
                long bfComp = bf.getMetrics().getComparisonCount();
                bfActual.add(L, bfComp);
                expected2N.add(L, Math.pow(2, L));
                fitted2N.add(L, cBrute * Math.pow(2, L));
            }
        }

        // === Combine all series into the dataset ===
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dynActual);
        dataset.addSeries(bfActual);
        dataset.addSeries(expectedN2);
        dataset.addSeries(expected2N);
        dataset.addSeries(fittedN2);
        dataset.addSeries(fitted2N);

        // === Build the chart ===
        JFreeChart chart = ChartFactory.createXYLineChart(
                "LCS Comparison Count vs Sequence Length",
                "Sequence Length (L)",
                "Character Comparisons",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // === Customize plot rendering ===
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        renderer.setSeriesPaint(0, Color.BLUE);     // Actual DP
        renderer.setSeriesPaint(1, Color.RED);      // Actual BF
        renderer.setSeriesPaint(2, Color.GREEN);    // Expected O(n²)
        renderer.setSeriesPaint(3, Color.ORANGE);   // Expected O(2ⁿ)
        renderer.setSeriesPaint(4, Color.CYAN);     // Fitted n²
        renderer.setSeriesPaint(5, Color.MAGENTA);  // Fitted 2ⁿ

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesLinesVisible(i, true);
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // === Annotate points ===
        for (LCSResult dyn : dynamicResults) {
            String label = dyn.getComparisonLabel();
            LCSResult bf = bruteMap.get(label);
            int L = extractLengthFromLabel(label);

            long dynComp = dyn.getMetrics().getComparisonCount();
            XYTextAnnotation dynNote = new XYTextAnnotation("Dyn: " + dynComp, L, dynComp);
            dynNote.setPaint(Color.BLUE);
            dynNote.setFont(new Font("SansSerif", Font.PLAIN, 10));
            dynNote.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
            plot.addAnnotation(dynNote);

            // Brute force annotation (only if data exists)
            if (bf != null) {
                long bfComp = bf.getMetrics().getComparisonCount();
                XYTextAnnotation bfNote = new XYTextAnnotation("BF: " + bfComp, L, bfComp);
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

    /**
     * Extracts the sequence length L from a label like "L30_P1".
     * Returns -1 if the format is invalid.
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
