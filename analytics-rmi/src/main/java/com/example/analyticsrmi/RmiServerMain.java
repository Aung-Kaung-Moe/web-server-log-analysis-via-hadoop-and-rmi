package com.example.analyticsrmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiServerMain {

    public static void main(String[] args) throws Exception {
        String bindHost = getenv("RMI_HOST", "analytics-rmi");
        int registryPort = Integer.parseInt(getenv("RMI_REGISTRY_PORT", "1099"));
        int exportPort   = Integer.parseInt(getenv("RMI_EXPORT_PORT", "2001"));

        // Important for Docker networking
        System.setProperty("java.rmi.server.hostname", bindHost);

        Registry registry = LocateRegistry.createRegistry(registryPort);

        AnalyticsService svc = new AnalyticsServiceImpl(exportPort);
        registry.rebind("AnalyticsService", svc);

        System.out.println("[RMI] bound AnalyticsService at " + bindHost + ":" + registryPort + " (exportPort=" + exportPort + ")");
        Thread.sleep(Long.MAX_VALUE);
    }

    private static String getenv(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.trim().isEmpty()) ? def : v.trim();
    }
}
