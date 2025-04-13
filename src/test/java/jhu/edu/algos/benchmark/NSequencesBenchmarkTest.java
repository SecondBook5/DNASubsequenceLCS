package jhu.edu.algos.benchmark;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for NSequencesBenchmark.
 * Validates:
 * - text report generation
 * - optional plot generation
 * - graceful handling of invalid CLI arguments
 * - presence of metrics in report
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NSequencesBenchmarkTest {

    private static final String TXT_PATH = "nsequences_benchmark.txt";
    private static final String PNG_PATH = "nsequences_plot.png";

    @BeforeEach
    void cleanBefore() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PNG_PATH));
    }

    @AfterEach
    void cleanAfter() throws IOException {
        Files.deleteIfExists(Path.of(TXT_PATH));
        Files.deleteIfExists(Path.of(PNG_PATH));
    }

    /**
     * Test: Run benchmark without plot.
     * Expected: TXT file should be created; PNG should not.
     */
    @Test
    void testRunWithoutPlotGeneratesTextOnly() throws Exception {
        // Quick run with plotting disabled
        NSequencesBenchmark.run(TXT_PATH, PNG_PATH, false);

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Text output file should be created.");
        assertFalse(Files.exists(Path.of(PNG_PATH)), "PNG should not be created without --plot.");

        List<String> lines = Files.readAllLines(Path.of(TXT_PATH));
        assertFalse(lines.isEmpty(), "Text file should not be empty.");

        // FIXED: Match the actual header line in the benchmark output
        assertTrue(lines.stream().anyMatch(line ->
                        line.contains("N-Sequences Benchmark Report")),
                "Should contain benchmark header.");

        boolean hasMetricsHeader = lines.stream().anyMatch(l ->
                l.contains("Dyn_Comparisons") && l.contains("BF_Comparisons"));
        assertTrue(hasMetricsHeader, "Header row with metric labels should be present.");

        boolean hasRowOfNumbers = lines.stream().anyMatch(l ->
                l.matches("^\\s*\\d+\\s+\\|\\s+\\d+.*\\d+.*\\|.*\\d+.*"));
        assertTrue(hasRowOfNumbers, "There should be at least one numeric summary row.");
    }


    /**
     * Test: Run benchmark with plot generation enabled.
     * Expected: Both TXT and PNG files should be generated.
     */
    @Test
    void testRunWithPlotGeneratesBothFiles() throws Exception {
        NSequencesBenchmark.run(TXT_PATH, PNG_PATH, true);

        assertTrue(Files.exists(Path.of(TXT_PATH)), "Text report file should be created.");
        assertTrue(Files.exists(Path.of(PNG_PATH)), "Plot PNG file should be created when --plot is enabled.");
    }

    /**
     * Test: Ensure CLI can handle invalid arguments gracefully.
     */
    @Test
    void testMainHandlesInvalidFlagGracefully() {
        assertDoesNotThrow(() -> NSequencesBenchmark.main(new String[]{"--unknown"}));
    }
}
