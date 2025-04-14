#!/bin/bash

# -------------------------------
# DNASubsequenceLCS Batch Runner
# -------------------------------
# Runs LCS comparisons on all input/*.txt files
# Runs both benchmarks (L-scaling and N-sequences)
# Logs entire execution to logs/full_run.log

# Create directories if needed
mkdir -p output
mkdir -p logs
mkdir -p input

LOG_FILE="logs/full_run.log"
EDGE_INPUT="input/edge_cases.txt"

echo "Creating edge case input: $EDGE_INPUT" | tee "$LOG_FILE"
cat > "$EDGE_INPUT" <<EOF
S1 =
S2 =
S3 = ACGTACGTAC
S4 = acgtacgtac
S5 = ACGT ACGT
S6 = ACGT-ACGT
S7 = ACGTACGTAC
S8 = ACGTACGTAC
EOF

# -------------------------------
# Run comparisons on all input files
# -------------------------------
echo "Running all LCS comparisons..." | tee -a "$LOG_FILE"
for infile in input/*.txt; do
  base=$(basename "$infile" .txt)
  outfile="output/${base}_results.txt"
  echo "  Comparing $base" | tee -a "$LOG_FILE"

  mvn exec:java \
    -Dexec.mainClass="jhu.edu.algos.Main" \
    -Dexec.args="$infile $outfile" \
    >> "$LOG_FILE" 2>&1
done

# -------------------------------
# Run L-scaling benchmark (two plots)
# -------------------------------
echo "Running L-Scaling Benchmark..." | tee -a "$LOG_FILE"
mvn exec:java \
  -Dexec.mainClass="jhu.edu.algos.Main" \
  -Dexec.args="benchmark-length scaling_benchmark.txt --plot" \
  >> "$LOG_FILE" 2>&1

# -------------------------------
# Run N-sequences benchmark (one plot)
# -------------------------------
echo "Running N-Sequences Benchmark..." | tee -a "$LOG_FILE"
mvn exec:java \
  -Dexec.mainClass="jhu.edu.algos.Main" \
  -Dexec.args="benchmark-n nsequences_benchmark.txt --plot" \
  >> "$LOG_FILE" 2>&1

# -------------------------------
# Summary
# -------------------------------
echo "" | tee -a "$LOG_FILE"
echo "DONE" | tee -a "$LOG_FILE"
echo "   Output directory:     output/" | tee -a "$LOG_FILE"
echo "   L-scaling plots:      scaling_plot_dynamic.png, scaling_plot_bruteforce.png" | tee -a "$LOG_FILE"
echo "   N-sequences plot:     nsequences_plot.png" | tee -a "$LOG_FILE"
echo "   Benchmark reports:    scaling_benchmark.txt, nsequences_benchmark.txt" | tee -a "$LOG_FILE"
echo "   Full log:             $LOG_FILE" | tee -a "$LOG_FILE"
