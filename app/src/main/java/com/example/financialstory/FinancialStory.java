package com.example.financialstory;

public class FinancialStory {
    private String financialStory;
    private long timestamp;

    public FinancialStory(String financialStory, long timestamp) {
        this.financialStory = financialStory;
        this.timestamp = timestamp;
    }

    public String getFinancialStory() {
        return financialStory;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
