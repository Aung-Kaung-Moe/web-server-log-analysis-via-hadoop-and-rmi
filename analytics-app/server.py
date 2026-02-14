#!/usr/bin/env python3
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import urlparse, parse_qs
from urllib.request import Request, urlopen
from urllib.error import HTTPError
import json

NAMENODE = "http://namenode:9870"  # inside docker network


def parse_kv_lines(txt, key_name="key", val_name="count"):
    items = []
    for line in txt.splitlines():
        line = line.strip()
        if not line:
            continue

        if "\t" in line:
            k, v = line.split("\t", 1)
        else:
            parts = line.split()
            if len(parts) < 2:
                continue
            k, v = parts[0], parts[1]

        try:
            v = int(v.strip())
        except Exception:
            continue

        items.append({key_name: k.strip(), val_name: v})

    items.sort(key=lambda x: x[val_name], reverse=True)
    return items


def webhdfs_open(path, user="root"):
    """
    WebHDFS OPEN returns 307 redirect to a datanode URL. We follow it manually.
    """
    url = "%s/webhdfs/v1%s?op=OPEN&user.name=%s" % (NAMENODE, path, user)
    req = Request(url, method="GET")
    try:
        r = urlopen(req, timeout=20)
        return r.read().decode("utf-8", "replace")
    except HTTPError as e:
        if e.code in (307, 302, 301) and "Location" in e.headers:
            loc = e.headers["Location"]
            r2 = urlopen(loc, timeout=20)
            return r2.read().decode("utf-8", "replace")
        raise


class Handler(BaseHTTPRequestHandler):
    def _send(self, code, body, content_type="application/json"):
        self.send_response(code)
        self.send_header("Content-Type", content_type)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()

        if isinstance(body, (dict, list)):
            body = json.dumps(body).encode("utf-8")
        elif isinstance(body, str):
            body = body.encode("utf-8")

        self.wfile.write(body)

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "*")
        self.end_headers()

    def do_GET(self):
        p = urlparse(self.path)

        # health
        if p.path == "/health":
            return self._send(200, {"ok": True})

        # html
        if p.path == "/":
            try:
                with open("/app/index.html", "rb") as f:
                    html = f.read()
                return self._send(200, html, "text/html; charset=utf-8")
            except Exception as ex:
                return self._send(500, {"error": "failed to load index.html", "detail": str(ex)})

        # busiest time
        if p.path == "/api/busiest_time":
            qs = parse_qs(p.query)
            date = (qs.get("date", [""])[0]).strip()
            if not date:
                return self._send(400, {"error": "missing date, example: /api/busiest_time?date=2026-02-08"})

            hdfs_file = "/analytics/busiest_time/date=%s/part-00000" % date
            try:
                txt = webhdfs_open(hdfs_file).strip()
            except Exception as ex:
                return self._send(500, {"error": "failed to read HDFS output", "path": hdfs_file, "detail": str(ex)})

            items = parse_kv_lines(txt, "bucket", "count")
            return self._send(200, {"date": date, "items": items})

        # top routes
        if p.path == "/api/top_routes":
            qs = parse_qs(p.query)
            date = (qs.get("date", [""])[0]).strip()
            limit = int((qs.get("limit", ["20"])[0]).strip() or "20")

            if not date:
                return self._send(400, {"error": "missing date, example: /api/top_routes?date=2026-02-08"})

            hdfs_file = "/analytics/top_routes/date=%s/part-00000" % date
            try:
                txt = webhdfs_open(hdfs_file).strip()
            except Exception as ex:
                return self._send(500, {"error": "failed to read HDFS output", "path": hdfs_file, "detail": str(ex)})

            items = parse_kv_lines(txt, "route", "count")[:limit]
            return self._send(200, {"date": date, "items": items})

        # most accessed
        if p.path == "/api/most_accessed":
            qs = parse_qs(p.query)
            date = (qs.get("date", [""])[0]).strip()
            limit = int((qs.get("limit", ["20"])[0]).strip() or "20")

            if not date:
                return self._send(400, {"error": "missing date, example: /api/most_accessed?date=2026-02-08"})

            hdfs_file = "/analytics/most_accessed/date=%s/part-00000" % date
            try:
                txt = webhdfs_open(hdfs_file).strip()
            except Exception as ex:
                return self._send(500, {"error": "failed to read HDFS output", "path": hdfs_file, "detail": str(ex)})

            items = parse_kv_lines(txt, "path", "count")[:limit]
            return self._send(200, {"date": date, "items": items})

        # summary (one call returns all)
        if p.path == "/api/summary":
            qs = parse_qs(p.query)
            date = (qs.get("date", [""])[0]).strip()
            limit = int((qs.get("limit", ["20"])[0]).strip() or "20")

            if not date:
                return self._send(400, {"error": "missing date, example: /api/summary?date=2026-02-08&limit=20"})

            out = {"date": date}
            try:
                txt = webhdfs_open("/analytics/busiest_time/date=%s/part-00000" % date).strip()
                out["busiest_time"] = parse_kv_lines(txt, "bucket", "count")
            except Exception as ex:
                out["busiest_time_error"] = str(ex)

            try:
                txt = webhdfs_open("/analytics/top_routes/date=%s/part-00000" % date).strip()
                out["top_routes"] = parse_kv_lines(txt, "route", "count")[:limit]
            except Exception as ex:
                out["top_routes_error"] = str(ex)

            try:
                txt = webhdfs_open("/analytics/most_accessed/date=%s/part-00000" % date).strip()
                out["most_accessed"] = parse_kv_lines(txt, "path", "count")[:limit]
            except Exception as ex:
                out["most_accessed_error"] = str(ex)

            return self._send(200, out)

        return self._send(404, {"error": "not found", "path": p.path})


if __name__ == "__main__":
    HTTPServer(("0.0.0.0", 8000), Handler).serve_forever()
