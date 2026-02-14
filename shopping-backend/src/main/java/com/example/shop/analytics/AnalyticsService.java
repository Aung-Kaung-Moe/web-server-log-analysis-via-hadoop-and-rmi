package com.example.shop.analytics;

import com.example.analyticsrmi.ResultRow;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final RmiAnalyticsClient rmi;

    public AnalyticsService(RmiAnalyticsClient rmi) {
        this.rmi = rmi;
    }

    public List<ResultRow> getTopEndpoints(String date, int limit) throws Exception {
        return rmi.topEndpoints(date, limit);
    }
}
