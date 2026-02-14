#!/usr/bin/env python3
import sys

for line in sys.stdin:
    line = line.strip()
    if not line:
        continue

    parts = line.split("|")
    if len(parts) < 4:
        continue

    path = parts[3].strip()
    if not path:
        continue

    if "?" in path:
        path = path.split("?", 1)[0]

    sys.stdout.write(path + "\t1\n")
