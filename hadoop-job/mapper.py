#!/usr/bin/env python3
import os, sys, re

# Try both env names
infile = os.environ.get("mapreduce_map_input_file") or os.environ.get("map_input_file") or ""
m = re.search(r'backend=([^/]+)/', infile)
backend = m.group(1) if m else "unknown"

for line in sys.stdin:
    line = line.strip()
    if not line:
        continue
    sys.stdout.write(backend + "\t1\n")