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
 * - .txt benchmark output
 * - graph generation (when requested)
 * - graceful handling of unexpected flags
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScalingBenchmarkTest {

    private static final String TXT_PATH = "scaling_benchmark.txt";
    private static final String PLOT_PATH = "scaling_plot.png";

    /**
     * Deletes benchmark output files before each test to ensure a clean environment.
     */
    @BeforeEach
    void deleteBefore() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PLOT_PATH));
    }

    /**
     * Deletes files after each test to avoid interference.
     */
    @AfterEach
    void deleteAfter() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PLOT_PATH));
    }

    /**
     * Tests that running the benchmark without plot generation
     * creates only the .txt output.
     */
    @Test
    void testRunWithoutPlotGeneratesTxtOnly() throws Exception {
        ScalingBenchmark.run(TXT_PATH, PLOT_PATH, false);

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Expected .txt benchmark file to be generated.");
        assertFalse(Files.exists(Path.of(PLOT_PATH)), "Plot image should not be generated without --plot flag.");

        List<String> lines = Files.readAllLines(Path.of(TXT_PATH));
        assertFalse(lines.isEmpty(), "Text file should not be empty.");
        assertTrue(lines.get(0).contains("Scaling Benchmark Report"), "Should include benchmark header.");
        assertTrue(lines.stream().anyMatch(l -> l.contains("Dyn_Comparisons")), "Should include header row.");
        assertTrue(lines.stream().anyMatch(l -> l.matches(".*\\d+.*")), "Should include numeric results.");
    }

    /**
     * Tests that enabling plot generation writes both .txt and .png files.
     */
    @Test
    void testRunWithPlotGeneratesTxtAndPlot() throws Exception {
        ScalingBenchmark.run(TXT_PATH, PLOT_PATH, true);

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Benchmark text file should be generated.");
        assertTrue(Files.exists(Path.of(PLOT_PATH)), "Graph image file should be generated.");
    }

    /**
     * Ensures that invalid command-line flags are safely ignored.
     */
    @Test
    void testMainHandlesUnknownFlagsGracefully() {
        assertDoesNotThrow(() -> ScalingBenchmark.main(new String[]{"--banana"}), "Unknown flags should not crash the program.");
    }
}
