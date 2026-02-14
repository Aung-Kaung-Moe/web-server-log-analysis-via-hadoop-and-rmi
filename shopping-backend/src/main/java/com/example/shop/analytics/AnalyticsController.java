package com.example.shop.analytics;

import com.example.analyticsrmi.ResultRow;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/top-endpoints")
    public List<ResultRow> topEndpoints(
            @RequestParam String date,
            @RequestParam(defaultValue = "10") int limit
    ) throws Exception {
        return analyticsService.getTopEndpoints(date, limit);
    }
}
