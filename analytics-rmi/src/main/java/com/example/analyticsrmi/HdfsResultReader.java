package com.example.analyticsrmi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HdfsResultReader {

    /**
     * Reads the file in HDFS by calling:
     *   hdfs dfs -cat <path>
     *
     * This avoids needing Hadoop client jars in your Java app because you're already in a hadoop-base container.
     */
    public List<String> cat(String hdfsPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("bash", "-lc", "hdfs dfs -cat '" + hdfsPath + "' 2>/dev/null || true");
        pb.redirectErrorStream(true);

        Process p = pb.start();

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }

        p.waitFor();
        return lines;
    }
}
