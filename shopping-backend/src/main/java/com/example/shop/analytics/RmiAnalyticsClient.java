package com.example.shop.analytics;

import com.example.analyticsrmi.AnalyticsService;
import com.example.analyticsrmi.ResultRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

@Component
public class RmiAnalyticsClient {

    @Value("${analytics.rmi.host:analytics-rmi}")
    private String host;

    @Value("${analytics.rmi.port:1099}")
    private int port;

    public List<ResultRow> topEndpoints(String date, int limit) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, port);
        AnalyticsService svc = (AnalyticsService) registry.lookup("AnalyticsService");
        return svc.topEndpoints(date, limit);
    }
}
