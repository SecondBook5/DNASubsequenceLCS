package jhu.edu.algos.benchmark;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for PairwiseBenchmark.
 * Validates:
 * - text report generation
 * - optional plot generation
 * - graceful handling of invalid CLI arguments
 * - presence of metrics in report
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PairwiseBenchmarkTest {

    private static final String TXT_PATH = "pairwise_benchmark.txt";
    private static final String PNG_PATH = "pairwise_plot.png";

    /**
     * Deletes output files before each test run to ensure a clean state.
     */
    @BeforeEach
    void cleanBefore() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PNG_PATH));
    }

    /**
     * Cleans up output files after each test run to avoid leftover files.
     */
    @AfterEach
    void cleanAfter() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PNG_PATH));
    }

    /**
     * Tests the benchmark with plotting disabled.
     * Verifies that the .txt file is created and the plot is not.
     */
    @Test
    void testRunWithoutPlotGeneratesTextOnly() throws Exception {
        // Using a smaller sequence length and a smaller step count for faster execution
        PairwiseBenchmark.run(TXT_PATH, PNG_PATH, false, 10, 10, 5);  // Small sample to run fast

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Text output file should be created.");
        assertFalse(Files.exists(Path.of(PNG_PATH)), "PNG should not be created without --plot.");

        List<String> lines = Files.readAllLines(Path.of(TXT_PATH));
        assertFalse(lines.isEmpty(), "Output text file should not be empty.");
        assertTrue(lines.get(0).contains("Pairwise Benchmark Report"), "Should contain header.");

        // Check that the metrics header is present
        boolean hasMetricsHeader = lines.stream().anyMatch(l ->
                l.contains("Dyn_Comparisons") && l.contains("BF_Comparisons"));
        assertTrue(hasMetricsHeader, "Metrics header should be present.");

        // Check that there are numeric results in the output
        boolean hasNumericResults = lines.stream().anyMatch(l ->
                l.matches(".*\\d+\\s+\\|.*\\d+.*"));
        assertTrue(hasNumericResults, "Some data rows should contain numeric results.");
    }

    /**
     * Tests the benchmark with plotting enabled.
     * Verifies that both .txt and .png files are created.
     */
    @Test
    void testRunWithPlotGeneratesBothFiles() throws Exception {
        // Lightweight run using smaller sequence length and step count
        PairwiseBenchmark.run(TXT_PATH, PNG_PATH, true, 10, 10, 5); // Lightweight run

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Text file should exist.");
        assertTrue(Files.exists(Path.of(PNG_PATH)), "Plot image should exist.");
    }

    /**
     * Ensures the CLI handles unknown flags without failure.
     */
    @Test
    void testMainHandlesInvalidFlagGracefully() {
        assertDoesNotThrow(() -> PairwiseBenchmark.main(new String[]{"--invalid"}));
    }

}
