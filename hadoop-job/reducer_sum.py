#!/usr/bin/env python3
import sys

cur_key = None
s = 0

for line in sys.stdin:
    line = line.strip()
    if not line:
        continue
    parts = line.split("\t")
    if len(parts) < 2:
        continue

    k = parts[0]
    try:
        v = int(parts[1])
    except:
        continue

    if cur_key is None:
        cur_key, s = k, v
    elif k == cur_key:
        s += v
    else:
        sys.stdout.write(cur_key + "\t" + str(s) + "\n")
        cur_key, s = k, v

if cur_key is not None:
    sys.stdout.write(cur_key + "\t" + str(s) + "\n")
