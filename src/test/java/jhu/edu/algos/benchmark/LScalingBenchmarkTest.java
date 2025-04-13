package jhu.edu.algos.benchmark;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for LScalingBenchmark.
 *
 * This suite verifies:
 * - Correct generation of benchmark text output (.txt)
 * - Optional generation of performance graph (.png)
 * - Graceful handling of invalid command-line input
 * - Presence and format of key benchmark metrics in the output
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LScalingBenchmarkTest {

    private static final String TXT_PATH = "scaling_benchmark.txt";
    private static final String PLOT_PATH = "scaling_plot.png";

    /**
     * Deletes output files before each test run to ensure a clean environment.
     */
    @BeforeEach
    void cleanBefore() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PLOT_PATH));
    }

    /**
     * Deletes generated files after each test to prevent test pollution.
     */
    @AfterEach
    void cleanAfter() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PLOT_PATH));
    }

    /**
     * Tests benchmark output with plotting disabled.
     * Ensures .txt is created and .png is not.
     */
    @Test
    void testRunWithoutPlotGeneratesTxtOnly() throws Exception {
        // Run with minimal input range to keep test duration short
        LScalingBenchmark.run(TXT_PATH, PLOT_PATH, false, 10, 20, 10, 2);

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Expected benchmark .txt to be generated.");
        assertFalse(Files.exists(Path.of(PLOT_PATH)), "No PNG should be generated without --plot.");

        List<String> lines = Files.readAllLines(Path.of(TXT_PATH));
        assertFalse(lines.isEmpty(), "Benchmark text output should not be empty.");
        assertTrue(lines.get(0).contains("Scaling Benchmark Report"), "Missing benchmark report header.");

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
     * Ensures both .txt and .png are generated.
     */
    @Test
    void testRunWithPlotGeneratesTxtAndPlot() throws Exception {
        LScalingBenchmark.run(TXT_PATH, PLOT_PATH, true, 10, 20, 10, 2);

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Expected .txt file to exist.");
        assertTrue(Files.exists(Path.of(PLOT_PATH)), "Expected .png graph to be created.");
    }

    /**
     * Verifies that invalid CLI flags do not crash the application.
     */
    @Test
    void testMainHandlesInvalidFlagGracefully() {
        assertDoesNotThrow(() -> LScalingBenchmark.main(new String[]{"--banana"}));
    }

    /**
     * Verifies that brute force results are skipped at unsafe lengths.
     * Expected: dashes ("-") in place of metrics, mixed with valid entries.
     */
    @Test
    void testBruteForceSkipMarkersAppear() throws Exception {
        // Efficient: Only L=10 and L=35, just 1 pair each
        LScalingBenchmark.run(TXT_PATH, PLOT_PATH, false, 10, 35, 25, 1);

        List<String> lines = Files.readAllLines(Path.of(TXT_PATH));

        // Expect skip markers at long lengths
        boolean hasSkip = lines.stream().anyMatch(line ->
                line.matches(".*\\|.*\\s+-\\s+.*-\\s*$")
        );

        // Expect valid brute force result at short lengths
        boolean hasBF = lines.stream().anyMatch(line ->
                line.matches(".*\\|.*\\d+.*\\|.*\\d+.*")
        );

        assertTrue(hasSkip, "Expected skipped brute force entries marked with '-' for longer sequences.");
        assertTrue(hasBF, "Expected at least one measured brute force value for shorter sequences.");
    }
}
