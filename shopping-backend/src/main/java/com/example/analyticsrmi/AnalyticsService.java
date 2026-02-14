package com.example.analyticsrmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AnalyticsService extends Remote {
    List<ResultRow> topEndpoints(String date, int limit) throws RemoteException;
    String ping() throws RemoteException;
}
