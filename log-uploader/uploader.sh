#!/usr/bin/env bash
set -euo pipefail

LOG_FILE="${LOG_FILE:-/logs/access.log}"
WEBHDFS_HOST="${WEBHDFS_HOST:-namenode}"
WEBHDFS_PORT="${WEBHDFS_PORT:-9870}"
HDFS_BASE_DIR="${HDFS_BASE_DIR:-/logs/shopping-backend}"
BACKEND_ID="${BACKEND_ID:-backend1}"

echo "[uploader] LOG_FILE=$LOG_FILE"
echo "[uploader] WEBHDFS=$WEBHDFS_HOST:$WEBHDFS_PORT"
echo "[uploader] HDFS_BASE_DIR=$HDFS_BASE_DIR BACKEND_ID=$BACKEND_ID"

while [ ! -f "$LOG_FILE" ]; do
  echo "[uploader] waiting for $LOG_FILE ..."
  sleep 2
done

# store batches per date
tmp_dir="/tmp/uploader_batches"
mkdir -p "$tmp_dir"

mkdir_hdfs_dir() {
  local d="$1"
  local hdfs_dir="${HDFS_BASE_DIR}/date=${d}/backend=${BACKEND_ID}"
  curl -sS -X PUT "http://${WEBHDFS_HOST}:${WEBHDFS_PORT}/webhdfs/v1${hdfs_dir}?op=MKDIRS&user.name=root" >/dev/null || true
}

upload_batch() {
  local d="$1"
  local tmp_file="${tmp_dir}/${d}.log"
  [ -s "$tmp_file" ] || return 0

  local hdfs_dir="${HDFS_BASE_DIR}/date=${d}/backend=${BACKEND_ID}"
  local ts="$(date +%H%M%S)"
  local hdfs_file="${hdfs_dir}/access_${ts}_$(date +%s).log"

  mkdir_hdfs_dir "$d"

  loc="$(curl -sS -i -X PUT "http://${WEBHDFS_HOST}:${WEBHDFS_PORT}/webhdfs/v1${hdfs_file}?op=CREATE&overwrite=true&user.name=root" \
    | tr -d '\r' | awk -F': ' '/^Location: /{print $2}' | tail -n 1)"
  loc="${loc}&user.name=root"

  if [ -z "$loc" ]; then
    echo "[uploader] ERROR: no Location from CREATE for $hdfs_file"
    : > "$tmp_file"
    return 0
  fi

  curl -sS -X PUT --data-binary @"$tmp_file" "$loc" >/dev/null
  echo "[uploader] uploaded $(wc -c < "$tmp_file") bytes -> $hdfs_file"
  : > "$tmp_file"
}

# flush loop
(
  while true; do
    sleep 5
    for f in "$tmp_dir"/*.log; do
      [ -e "$f" ] || continue
      d="$(basename "$f" .log)"
      upload_batch "$d" || true
    done
  done
) &

echo "[uploader] tailing (from end) ..."
tail -n 0 -F "$LOG_FILE" | while IFS= read -r line; do
  [ -n "$line" ] || continue

  # extract date from timestamp at start of line
  ts="${line%%|*}"         # before first |
  d="${ts%%T*}"            # before T

  # only accept YYYY-MM-DD
  if echo "$d" | grep -Eq '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'; then
    echo "$line" >> "${tmp_dir}/${d}.log"
  else
    # ignore bad lines
    :
  fi
done
