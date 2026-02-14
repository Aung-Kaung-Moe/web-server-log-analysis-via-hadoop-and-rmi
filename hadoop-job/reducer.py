#!/usr/bin/env python3
import sys

cur = None
s = 0

for line in sys.stdin:
    line = line.strip()
    if not line:
        continue
    parts = line.split("\t", 1)
    if len(parts) != 2:
        continue
    k = parts[0]
    v = int(parts[1])

    if cur is None:
        cur = k
        s = v
    elif k == cur:
        s += v
    else:
        sys.stdout.write(cur + "\t" + str(s) + "\n")
        cur = k
        s = v

if cur is not None:
    sys.stdout.write(cur + "\t" + str(s) + "\n")