package jhu.edu.algos.benchmark;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for LScalingBenchmark with dual-plot PNG outputs.
 *
 * Verifies:
 * - Output text generation
 * - Proper plot splitting into dynamic and brute-force PNGs
 * - Graceful error handling
 * - Logical formatting of benchmark data
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LScalingBenchmarkTest {

    private static final String TXT_PATH = "scaling_benchmark.txt";
    private static final String PLOT_BASE = "scaling_plot.png";
    private static final String DYNAMIC_PNG = "scaling_plot_dynamic.png";
    private static final String BRUTEFORCE_PNG = "scaling_plot_bruteforce.png";

    /**
     * Deletes output files before each test run to ensure a clean environment.
     */
    @BeforeEach
    void cleanBefore() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PLOT_BASE));
        Files.deleteIfExists(Path.of(DYNAMIC_PNG));
        Files.deleteIfExists(Path.of(BRUTEFORCE_PNG));
    }

    /**
     * Deletes output files after test completion to prevent test pollution.
     */
    @AfterEach
    void cleanAfter() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PLOT_BASE));
        Files.deleteIfExists(Path.of(DYNAMIC_PNG));
        Files.deleteIfExists(Path.of(BRUTEFORCE_PNG));
    }

    /**
     * Tests benchmark output with plotting disabled.
     * Ensures .txt is created and no PNG files are generated.
     */
    @Test
    void testRunWithoutPlotGeneratesTxtOnly() throws Exception {
        LScalingBenchmark.run(TXT_PATH, PLOT_BASE, false, 10, 20, 10, 2);

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Expected benchmark .txt to be generated.");
        assertFalse(Files.exists(Path.of(DYNAMIC_PNG)), "No DP PNG should be generated without --plot.");
        assertFalse(Files.exists(Path.of(BRUTEFORCE_PNG)), "No BF PNG should be generated without --plot.");

        List<String> lines = Files.readAllLines(Path.of(TXT_PATH));
        assertFalse(lines.isEmpty(), "Benchmark text output should not be empty.");
        assertTrue(lines.get(0).contains("Benchmark Report"), "Missing benchmark report header.");

        // Check for metrics table headers
        boolean hasMetricHeaders = lines.stream().anyMatch(line ->
                line.contains("Dyn_Comparisons") && line.contains("BF_Comparisons"));
        assertTrue(hasMetricHeaders, "Missing expected header columns.");

        // At least one line should have numeric output
        boolean hasNumericResults = lines.stream().anyMatch(line ->
                line.matches(".*\\|\\s+Pair_\\d+.*\\d+.*\\d+.*"));
        assertTrue(hasNumericResults, "Should include numeric results in output rows.");
    }

    /**
     * Tests benchmark output with plotting enabled.
     * Ensures both .txt and both .png files are created.
     */
    @Test
    void testRunWithPlotGeneratesTxtAndDualPlots() throws Exception {
        LScalingBenchmark.run(TXT_PATH, PLOT_BASE, true, 10, 20, 10, 2);

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Expected .txt file to exist.");
        assertTrue(Files.exists(Path.of(DYNAMIC_PNG)), "Expected dynamic PNG to be created.");
        assertTrue(Files.exists(Path.of(BRUTEFORCE_PNG)), "Expected brute force PNG to be created.");
    }

    /**
     * Verifies that invalid CLI flags do not crash the application.
     */
    @Test
    void testMainHandlesInvalidFlagGracefully() {
        assertDoesNotThrow(() -> LScalingBenchmark.main(new String[]{"--banana"}));
    }

    /**
     * Verifies that brute force results are skipped when sequences are too long.
     */
    @Test
    void testBruteForceSkipMarkersAppear() throws Exception {
        LScalingBenchmark.run(TXT_PATH, PLOT_BASE, false, 10, 35, 25, 1);

        List<String> lines = Files.readAllLines(Path.of(TXT_PATH));

        // Expect '-' marker where BF is skipped
        boolean hasSkip = lines.stream().anyMatch(line ->
                line.matches(".*\\|.*\\s+-\\s+.*-\\s*$"));

        // Expect some brute force results at shorter lengths
        boolean hasBF = lines.stream().anyMatch(line ->
                line.matches(".*\\|.*\\d+.*\\|.*\\d+.*"));

        assertTrue(hasSkip, "Expected skipped brute force entries marked with '-' for long L.");
        assertTrue(hasBF, "Expected at least one measured brute force value for short L.");
    }
}
