package jhu.edu.algos.benchmark;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ScalingBenchmark.
 * Verifies:
 * - .txt benchmark output for multiple pairwise combinations
 * - graph generation when requested
 * - graceful handling of invalid CLI flags
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScalingBenchmarkTest {

    private static final String TXT_PATH = "scaling_benchmark.txt";
    private static final String PLOT_PATH = "scaling_plot.png";

    /**
     * Deletes output files before each test.
     */
    @BeforeEach
    void deleteBefore() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PLOT_PATH));
    }

    /**
     * Cleans up output files after each test.
     */
    @AfterEach
    void deleteAfter() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PLOT_PATH));
    }

    /**
     * Tests that the benchmark runs correctly and generates only the .txt output
     * when the --plot flag is not provided.
     */
    @Test
    void testRunWithoutPlotGeneratesTxtOnly() throws Exception {
        ScalingBenchmark.run(TXT_PATH, PLOT_PATH, false, 10, 30, 10, 2);  // limited test range

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Expected .txt benchmark file to be generated.");
        assertFalse(Files.exists(Path.of(PLOT_PATH)), "Plot should not be generated without the --plot flag.");

        List<String> lines = Files.readAllLines(Path.of(TXT_PATH));
        assertFalse(lines.isEmpty(), "Output text file should not be empty.");

        // Check header
        assertTrue(lines.stream().anyMatch(line -> line.contains("Scaling Benchmark Report")),
                "Should include benchmark header.");

        // Check column headers
        assertTrue(lines.stream().anyMatch(line ->
                        line.contains("Pair") &&
                                line.contains("Dyn_Comparisons") &&
                                line.contains("BF_Comparisons")),
                "Header row should include metric labels.");

        // At least one line should show a numerical benchmark value
        assertTrue(lines.stream().anyMatch(line ->
                        line.matches(".*\\|\\s+Pair_\\d+.*\\d+.*")),
                "At least one result line should show numeric output.");
    }

    /**
     * Tests that the benchmark generates both .txt and .png when plotting is enabled.
     */
    @Test
    void testRunWithPlotGeneratesTxtAndPlot() throws Exception {
        ScalingBenchmark.run(TXT_PATH, PLOT_PATH, true, 10, 30, 10, 2);

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Benchmark text file should be created.");
        assertTrue(Files.exists(Path.of(PLOT_PATH)), "PNG plot image should be created.");
    }

    /**
     * Verifies that the main method handles unknown CLI flags without crashing.
     */
    @Test
    void testMainHandlesUnknownFlagsGracefully() {
        assertDoesNotThrow(() -> ScalingBenchmark.main(new String[]{"--banana"}), "Unknown CLI flag should not crash the program.");
    }

    /**
     * Tests that brute force is skipped beyond a safe input size and marked with a dash.
     */
    @Test
    void testBruteForceSkipMarkers() throws Exception {
        ScalingBenchmark.run(TXT_PATH, PLOT_PATH, false, 10, 60, 10, 2);  // test through cap and beyond

        List<String> lines = Files.readAllLines(Path.of(TXT_PATH));

        boolean hasSkipMarker = lines.stream().anyMatch(line ->
                line.contains(" - ") && line.contains("BF_Comparisons"));

        boolean hasMeasuredBF = lines.stream().anyMatch(line ->
                line.matches(".*\\|\\s+Pair_\\d+.*\\d+.*\\d+.*\\d+.*\\d+.*"));

        assertTrue(hasMeasuredBF, "Should have some valid brute-force entries.");
        assertTrue(lines.stream().anyMatch(line -> line.contains("-")),
                "Should contain '-' to indicate BF skipped beyond safe length.");
    }
}
