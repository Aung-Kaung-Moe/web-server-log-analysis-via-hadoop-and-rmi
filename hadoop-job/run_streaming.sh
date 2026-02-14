#!/usr/bin/env bash
set -euo pipefail

DATE="${1:-$(date +%F)}"

INPUT="/logs/shopping-backend/date=${DATE}"
OUTPUT="/analytics/most_accessed/date=${DATE}"

echo "[job] input:  ${INPUT}"
echo "[job] output: ${OUTPUT}"

hdfs dfs -test -d "${INPUT}" || { echo "Input not found in HDFS: ${INPUT}"; exit 1; }
hdfs dfs -rm -r -f "${OUTPUT}" || true

hadoop jar /opt/hadoop/share/hadoop/tools/lib/hadoop-streaming-*.jar \
  -input "${INPUT}" \
  -output "${OUTPUT}" \
  -mapper "python3 mapper.py" \
  -reducer "python3 reducer.py" \
  -file mapper.py \
  -file reducer.py

echo "[job] done. top results:"
hdfs dfs -cat "${OUTPUT}/part-00000" | sort -k2 -nr | head -n 20
