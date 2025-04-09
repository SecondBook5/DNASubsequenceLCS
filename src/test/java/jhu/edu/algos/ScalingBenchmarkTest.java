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
     * Tests that running the benchmark without the --plot flag
     * generates only the CSV file and no graph image.
     */
    @Test
    void testBenchmarkWithoutPlotGeneratesCSV() throws Exception {
        ScalingBenchmark.main(new String[]{});

        assertTrue(Files.exists(Path.of(CSV_PATH)), "Expected CSV file to be generated.");
        assertFalse(Files.exists(Path.of(PLOT_PATH)), "Graph should not be generated without --plot.");

        List<String> lines = Files.readAllLines(Path.of(CSV_PATH));
        assertFalse(lines.isEmpty(), "CSV should contain at least the header.");
        assertTrue(lines.get(0).startsWith("Length,Dynamic_Comparisons"), "Header should be present and correct.");
        assertTrue(lines.size() > 1, "CSV should contain at least one row of results.");
    }

    /**
     * Tests that the --plot flag causes a plot to be created in addition to CSV.
     */
    @Test
    void testBenchmarkWithPlotGeneratesGraph() throws Exception {
        ScalingBenchmark.main(new String[]{"--plot"});

        assertTrue(Files.exists(Path.of(CSV_PATH)), "CSV file should be generated.");
        assertTrue(Files.exists(Path.of(PLOT_PATH)), "Graph image should be generated when using --plot.");
    }

    /**
     * Ensures that unrecognized flags do not cause crashes or exceptions.
     */
    @Test
    void testBenchmarkHandlesInvalidFlagsGracefully() {
        assertDoesNotThrow(() -> ScalingBenchmark.main(new String[]{"--banana"}), "Unknown flags should not cause crashes.");
    }
}
