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
 * Generates a graph of comparison counts vs. number of pairwise LCS computations.
 * This class is used to evaluate how performance scales with the number of sequence pairs
 * when sequence length is fixed. It visualizes linear behavior for dynamic programming and brute force.
 */
public class PairwiseGraphGenerator {

    /**
     * Entry method to generate a comparative benchmark graph.
     *
     * @param dynResults List of dynamic programming LCSResult objects.
     * @param bfResults  List of brute force LCSResult objects (may include nulls for skipped tests).
     * @param outputPath Output PNG file path for the resulting chart.
     */
    public static void generateGraph(List<LCSResult> dynResults,
                                     List<LCSResult> bfResults,
                                     String outputPath) {

        // === Defensive Null Check ===
        if (dynResults == null || bfResults == null) {
            System.err.println("Error: Result lists are null.");
            return;
        }

        // === Chunk results into groups of 5 comparisons ===
        // Each group represents a fixed number of comparisons (e.g., 5, 10, 15...) at fixed sequence length.
        Map<Integer, Long> dynSums = new TreeMap<>();
        Map<Integer, Long> bfSums = new TreeMap<>();
        int chunkSize = 5;

        for (int i = 0; i < dynResults.size(); i++) {
            int group = ((i + 1) / chunkSize) * chunkSize;
            if (group == 0) group = chunkSize;

            dynSums.put(group,
                    dynSums.getOrDefault(group, 0L) + dynResults.get(i).getMetrics().getComparisonCount());

            if (bfResults.get(i) != null) {
                bfSums.put(group,
                        bfSums.getOrDefault(group, 0L) + bfResults.get(i).getMetrics().getComparisonCount());
            }
        }

        // === Fit T(p) ≈ c·p using linear least-squares ===
        List<Double> dynX = new ArrayList<>();
        List<Double> dynY = new ArrayList<>();
        dynSums.forEach((x, y) -> {
            dynX.add(x.doubleValue());
            dynY.add(y.doubleValue());
        });
        double dynFit = CurveFitter.fitLinear(dynX, dynY);

        List<Double> bfX = new ArrayList<>();
        List<Double> bfY = new ArrayList<>();
        bfSums.forEach((x, y) -> {
            bfX.add(x.doubleValue());
            bfY.add(y.doubleValue());
        });
        double bfFit = CurveFitter.fitLinear(bfX, bfY);

        // === Print fit models to terminal ===
        System.out.printf("%n==== Fitted Models ====%n");
        System.out.printf("Dynamic Programming: T(p) ≈ %.5f · p%n", dynFit);
        if (!bfX.isEmpty()) {
            System.out.printf("Brute Force        : T(p) ≈ %.5f · p%n", bfFit);
        }
        System.out.println("=======================\n");

        // === Create data series for actual and fitted comparisons ===
        XYSeries dynActual = new XYSeries("Actual: Dynamic");
        XYSeries bfActual = new XYSeries("Actual: Brute Force");
        XYSeries dynFitted = new XYSeries("Fitted: c·p (Dyn)");
        XYSeries bfFitted = new XYSeries("Fitted: c·p (BF)");
        XYSeries expectedLinear = new XYSeries("Expected: O(p)");

        // Dynamic points: actual, fit, expected baseline
        for (Integer p : dynSums.keySet()) {
            double pd = p.doubleValue();
            double val = dynSums.get(p).doubleValue();
            dynActual.add(pd, val);
            dynFitted.add(pd, dynFit * pd);
            expectedLinear.add(pd, pd * 100.0); // Arbitrary baseline
        }

        // Brute force actual and fitted points
        for (Integer p : bfSums.keySet()) {
            double pd = p.doubleValue();
            double val = bfSums.get(p).doubleValue();
            bfActual.add(pd, val);
            bfFitted.add(pd, bfFit * pd);
        }

        // === Combine series into dataset ===
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dynActual);
        dataset.addSeries(bfActual);
        dataset.addSeries(expectedLinear);
        dataset.addSeries(dynFitted);
        dataset.addSeries(bfFitted);

        // === Configure chart ===
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Comparison Scaling vs Number of Pairs (Fixed Length)",
                "Number of Sequence Pairs",
                "Total Comparisons",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Color assignments per series
        renderer.setSeriesPaint(0, Color.BLUE);     // Dynamic Actual
        renderer.setSeriesPaint(1, Color.RED);      // Brute Force Actual
        renderer.setSeriesPaint(2, Color.GRAY);     // Expected O(p)
        renderer.setSeriesPaint(3, Color.CYAN);     // Fitted Dynamic
        renderer.setSeriesPaint(4, Color.MAGENTA);  // Fitted Brute

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesShapesVisible(i, true);
            renderer.setSeriesLinesVisible(i, true);
        }

        // Set plot theme
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // === Add annotations for each point ===
        dynSums.forEach((p, val) -> {
            XYTextAnnotation note = new XYTextAnnotation("Dyn: " + val, p.doubleValue(), val.doubleValue());
            note.setFont(new Font("SansSerif", Font.PLAIN, 9));
            note.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
            plot.addAnnotation(note);
        });

        bfSums.forEach((p, val) -> {
            XYTextAnnotation note = new XYTextAnnotation("BF: " + val, p.doubleValue(), val.doubleValue());
            note.setFont(new Font("SansSerif", Font.PLAIN, 9));
            note.setTextAnchor(TextAnchor.TOP_LEFT);
            plot.addAnnotation(note);
        });

        // === Save PNG file ===
        try {
            ChartUtils.saveChartAsPNG(new File(outputPath), chart, 1000, 650);
            System.out.println("Graph saved to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error saving graph: " + e.getMessage());
        }
    }
}
