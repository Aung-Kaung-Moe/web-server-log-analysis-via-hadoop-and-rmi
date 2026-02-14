package com.example.analyticsrmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnalyticsServiceImpl extends UnicastRemoteObject implements AnalyticsService {

    private final HdfsResultReader reader = new HdfsResultReader();

    public AnalyticsServiceImpl(int exportPort) throws RemoteException {
        super(exportPort);
    }

    @Override
    public List<ResultRow> topEndpoints(String date, int limit) throws RemoteException {
        try {
            String path = "/analytics/most_accessed/date=" + date + "/part-00000";
            List<String> raw = reader.cat(path);

            // Parse lines like: "      2 /auth/login"
            List<ResultRow> rows = new ArrayList<>();
            for (String line : raw) {
                String trimmed = line.trim();

                // Skip Hadoop info lines if any ever sneak in
                if (trimmed.isEmpty()) continue;
                if (trimmed.contains("INFO sasl.SaslDataTransferClient")) continue;

                // Expect: "<count> <endpoint>"
                String[] parts = trimmed.split("\\s+", 2);
                if (parts.length < 2) continue;

                long count;
                try { count = Long.parseLong(parts[0]); }
                catch (NumberFormatException e) { continue; }

                String endpoint = parts[1].trim();
                if (!endpoint.startsWith("/")) continue;

                rows.add(new ResultRow(endpoint, count));
            }

            rows.sort(Comparator.comparingLong(ResultRow::getCount).reversed());
            if (limit <= 0) limit = 10;
            if (rows.size() > limit) return new ArrayList<>(rows.subList(0, limit));
            return rows;

        } catch (Exception e) {
            throw new RemoteException("Failed reading HDFS output", e);
        }
    }

    @Override
    public String ping() throws RemoteException {
        return "pong";
    }
}
