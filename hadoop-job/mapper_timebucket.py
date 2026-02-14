#!/usr/bin/env python3
import sys, re
from datetime import datetime, timedelta, tzinfo

class FixedOffset(tzinfo):
    def __init__(self, minutes):
        self._offset = timedelta(minutes=minutes)
    def utcoffset(self, dt):
        return self._offset
    def dst(self, dt):
        return timedelta(0)
    def tzname(self, dt):
        total = int(self._offset.total_seconds() // 60)
        sign = '+' if total >= 0 else '-'
        total = abs(total)
        return '%s%02d:%02d' % (sign, total//60, total%60)

MM_TZ = FixedOffset(6*60 + 30)

TS_RE = re.compile(
    r'^(\d{4}-\d{2}-\d{2})T'
    r'(\d{2}):(\d{2}):(\d{2})'
    r'(?:\.(\d+))?'
    r'(Z|([+-])(\d{2}):(\d{2}))?$'
)

def bucket_for_hour(h):
    if 5 <= h <= 11:
        return "morning"
    if 12 <= h <= 16:
        return "afternoon"
    if 17 <= h <= 20:
        return "evening"
    return "night"

def parse_ts(ts):
    m = TS_RE.match(ts.strip())
    if not m:
        return None

    ymd = m.group(1)
    hh = int(m.group(2)); mm = int(m.group(3)); ss = int(m.group(4))
    frac = m.group(5) or "0"
    frac = (frac + "000000")[:6]
    us = int(frac)

    z = m.group(6)
    sign = m.group(7) or '+'
    off_hh = int(m.group(8) or 0)
    off_mm = int(m.group(9) or 0)

    if z == 'Z':
        src_tz = FixedOffset(0)
    else:
        off = off_hh * 60 + off_mm
        if sign == '-':
            off = -off
        src_tz = FixedOffset(off)

    dt = datetime.strptime(ymd, "%Y-%m-%d")
    dt = dt.replace(hour=hh, minute=mm, second=ss, microsecond=us)
    dt = dt.replace(tzinfo=src_tz)
    return dt

for line in sys.stdin:
    line = line.strip()
    if not line:
        continue

    ts = line.split("|", 1)[0].strip()
    dt_src = parse_ts(ts)
    if not dt_src:
        continue

    dt_mm = dt_src.astimezone(MM_TZ)
    b = bucket_for_hour(dt_mm.hour)
    sys.stdout.write("%s\t1\n" % b)
