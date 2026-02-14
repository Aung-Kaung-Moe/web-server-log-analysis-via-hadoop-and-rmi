package com.example.analyticsrmi;

import java.io.Serializable;

public class ResultRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String key;
    private final long count;

    public ResultRow(String key, long count) {
        this.key = key;
        this.count = count;
    }

    public String getKey() { return key; }
    public long getCount() { return count; }
}
