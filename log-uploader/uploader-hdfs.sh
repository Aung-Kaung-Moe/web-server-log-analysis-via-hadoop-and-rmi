#!/usr/bin/env bash
set -euo pipefail

export PATH=/opt/hadoop-3.2.1/bin:/opt/hadoop-3.2.1/sbin:$PATH

: "${LOG_FILE:?}"
: "${HDFS_BASE_DIR:?}"
: "${BACKEND_ID:?}"
: "${TZ:=Asia/Yangon}"   # ensure container has TZ

TMPDIR=/tmp/logsplit
rm -rf "$TMPDIR"
mkdir -p "$TMPDIR"

# Split by LOCAL date derived from the ISO8601 timestamp (Z or with offset)
# Example ts: 2026-02-11T16:26:32.282473506Z
while IFS= read -r line; do
  ts="${line%%|*}"  # part before first '|'

  # Convert timestamp -> local date (YYYY-MM-DD)
  # GNU date can parse ISO8601 with Z
  day="$(TZ="$TZ" date -d "$ts" +%F 2>/dev/null || true)"
  if [[ -z "$day" ]]; then
    # if a bad line, skip or put in "unknown"
    continue
  fi

  echo "$line" >> "$TMPDIR/access.$day.log"
done < "$LOG_FILE"

# Upload each day's file to its own HDFS partition
shopt -s nullglob
for f in "$TMPDIR"/access.*.log; do
  base="$(basename "$f")"
  DATE="${base#access.}"
  DATE="${DATE%.log}"

  HDFS_DIR="${HDFS_BASE_DIR}/date=${DATE}/backend=${BACKEND_ID}"
  hdfs dfs -mkdir -p "$HDFS_DIR"
  hdfs dfs -put -f "$f" "${HDFS_DIR}/access.log"
done
