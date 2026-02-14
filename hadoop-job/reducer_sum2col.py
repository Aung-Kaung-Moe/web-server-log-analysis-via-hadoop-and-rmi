#!/usr/bin/env python3
import sys

cur_type = None
cur_key = None
s = 0

for line in sys.stdin:
    line = line.strip()
    if not line:
        continue

    parts = line.split("\t")
    if len(parts) < 3:
        continue

    t = parts[0]
    k = parts[1]
    try:
        v = int(parts[2])
    except:
        continue

    if cur_type is None:
        cur_type, cur_key, s = t, k, v
    elif t == cur_type and k == cur_key:
        s += v
    else:
        sys.stdout.write(cur_type + "\t" + cur_key + "\t" + str(s) + "\n")
        cur_type, cur_key, s = t, k, v

if cur_type is not None:
    sys.stdout.write(cur_type + "\t" + cur_key + "\t" + str(s) + "\n")