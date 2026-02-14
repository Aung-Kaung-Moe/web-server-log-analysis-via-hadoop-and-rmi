package com.example.analyticsrmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AnalyticsService extends Remote {

    /**
     * Reads: /analytics/most_accessed/date=<date>/part-00000
     * Returns top N endpoint counts.
     */
    List<ResultRow> topEndpoints(String date, int limit) throws RemoteException;

    /**
     * Simple health check
     */
    String ping() throws RemoteException;
}
