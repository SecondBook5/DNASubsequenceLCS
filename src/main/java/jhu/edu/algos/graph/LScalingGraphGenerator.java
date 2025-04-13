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
public class LScalingGraphGenerator {

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
        System.out.printf("Dynamic Programming: T(L) ≈ %.5f · L²%n", cDynamic);
        if (!cleanedBrute.isEmpty()) {
            System.out.printf("Brute Force        : T(L) ≈ %.5f · 2^L%n", cBrute);
        } else {
            System.out.println("Brute Force        : No data available for fitting.");
        }
        System.out.println("================================\n");

        // === Prepare chart series ===
        XYSeries dynActual = new XYSeries("Observed: Dynamic Programming");
        XYSeries bfActual = new XYSeries("Observed: Brute Force");
        XYSeries expectedN2 = new XYSeries("Theoretical: O(L²)");
        XYSeries expected2N = new XYSeries("Theoretical: O(2^L)");
        XYSeries fittedN2 = new XYSeries("Fitted: c·L²");
        XYSeries fitted2N = new XYSeries("Fitted: c·2^L");

        for (LCSResult dyn : dynamicResults) {
            String label = dyn.getComparisonLabel();
            LCSResult bf = bruteMap.get(label);

            // === Extract L from label pattern like "L30_P1" ===
            int L = extractLengthFromLabel(label);
            if (L == -1) continue;

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

        // === Combine all series into dataset ===
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dynActual);
        dataset.addSeries(bfActual);
        dataset.addSeries(expectedN2);
        dataset.addSeries(expected2N);
        dataset.addSeries(fittedN2);
        dataset.addSeries(fitted2N);

        // === Build the chart ===
        JFreeChart chart = ChartFactory.createXYLineChart(
                "LCS Comparison Count vs Sequence Length (L)",
                "Sequence Length (L)",
                "Total Character Comparisons",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // Add chart subtitle
        chart.addSubtitle(new TextTitle("Observed vs Theoretical Scaling of LCS Algorithms"));

        // === Customize chart aesthetics ===
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Assign consistent, colorblind-friendly colors
        renderer.setSeriesPaint(0, new Color(0x1f78b4)); // blue Actual: Dynamic
        renderer.setSeriesPaint(1, new Color(0xe31a1c)); // red Actual: Brute Force
        renderer.setSeriesPaint(2, new Color(0x33a02c)); // green Expected: O(n²)
        renderer.setSeriesPaint(3, new Color(0xff7f00)); // orange Expected: O(2^L)
        renderer.setSeriesPaint(4, new Color(0x6a3d9a)); // purple Fitted: c·L²
        renderer.setSeriesPaint(5, new Color(0xb15928)); // brown Fitted: c·2^L
        // Enable both lines and data points
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, true);
        }

        // Apply renderer and theme
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // === Annotate data points ===
        for (LCSResult dyn : dynamicResults) {
            String label = dyn.getComparisonLabel();
            LCSResult bf = bruteMap.get(label);
            int L = extractLengthFromLabel(label);

            long dynComp = dyn.getMetrics().getComparisonCount();
            XYTextAnnotation dynNote = new XYTextAnnotation("Dyn: " + dynComp, L, dynComp);
            dynNote.setPaint(new Color(0x1f78b4));
            dynNote.setFont(new Font("SansSerif", Font.PLAIN, 9));
            dynNote.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
            plot.addAnnotation(dynNote);

            // Brute force annotation (only if data exists)
            if (bf != null) {
                long bfComp = bf.getMetrics().getComparisonCount();
                XYTextAnnotation bfNote = new XYTextAnnotation("BF: " + bfComp, L, bfComp);
                bfNote.setPaint(new Color(0xe31a1c));
                bfNote.setFont(new Font("SansSerif", Font.PLAIN, 9));
                bfNote.setTextAnchor(TextAnchor.BOTTOM_LEFT);
                plot.addAnnotation(bfNote);
            }
        }

        // === Export as high-resolution PNG ===
        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 1200, 800);
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
