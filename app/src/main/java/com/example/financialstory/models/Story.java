package com.example.financialstory.models;

public class Story {
    private String content;

    public Story() {
        // Required empty constructor for Firebase
    }

    public Story(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
