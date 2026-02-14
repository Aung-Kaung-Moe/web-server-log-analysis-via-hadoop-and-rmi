package com.example.analyticsrmi;

import java.io.Serializable;

public class ResultRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String key;     // e.g. "/auth/login"
    private final long count;     // e.g. 42

    public ResultRow(String key, long count) {
        this.key = key;
        this.count = count;
    }

    public String getKey() { return key; }
    public long getCount() { return count; }

    @Override
    public String toString() {
        return count + " " + key;
    }
}
