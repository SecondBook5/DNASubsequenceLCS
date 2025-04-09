package jhu.edu.algos;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ScalingBenchmark.
 * Verifies:
 * - CSV generation
 * - Graph generation (when requested)
 * - Graceful handling of unexpected flags
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScalingBenchmarkTest {

    private static final String CSV_PATH = "scaling_benchmark.csv";
    private static final String PLOT_PATH = "scaling_plot.png";

    /**
     * Deletes benchmark output files before each test to ensure clean environment.
     */
    @BeforeEach
    void deleteBefore() throws IOException {
        Files.deleteIfExists(Path.of(CSV_PATH));
        Files.deleteIfExists(Path.of(PLOT_PATH));
    }

    /**
     * Deletes files after each test to avoid test interference.
     */
    @AfterEach
    void deleteAfter() throws IOException {
        Files.deleteIfExists(Path.of(CSV_PATH));
        Files.deleteIfExists(Path.of(PLOT_PATH));
    }

    /**
     * Tests that running the benchmark without plot generation
     * creates only the CSV file.
     */
    @Test
    void testRunWithoutPlotGeneratesCSVOnly() throws Exception {
        ScalingBenchmark.run(CSV_PATH, PLOT_PATH, false);

        assertTrue(Files.exists(Path.of(CSV_PATH)), "Expected CSV file to be generated.");
        assertFalse(Files.exists(Path.of(PLOT_PATH)), "Plot image should not be generated without flag.");

        List<String> lines = Files.readAllLines(Path.of(CSV_PATH));
        assertFalse(lines.isEmpty(), "CSV should contain content.");
        assertTrue(lines.get(0).startsWith("Length,Dynamic_Comparisons"), "CSV should have correct header.");
        assertTrue(lines.size() > 1, "CSV should have result rows.");
    }

    /**
     * Tests that plot generation flag produces both CSV and PNG outputs.
     */
    @Test
    void testRunWithPlotGeneratesBothOutputs() throws Exception {
        ScalingBenchmark.run(CSV_PATH, PLOT_PATH, true);

        assertTrue(Files.exists(Path.of(CSV_PATH)), "CSV file should be generated.");
        assertTrue(Files.exists(Path.of(PLOT_PATH)), "Plot image should be generated.");
    }

    /**
     * Tests that unknown flags passed to main() do not break execution.
     */
    @Test
    void testMainHandlesUnknownFlagsSafely() {
        assertDoesNotThrow(() -> ScalingBenchmark.main(new String[]{"--banana"}));
    }
}
