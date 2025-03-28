package com.example.financialstory;

public class NewsItem {
    private String title;
    private String description;
    private String content;
    private String url;
    private String imageUrl;
    private String publishedAt;
    private String sourceName;

    public NewsItem(String title, String description, String content, String url,
                    String imageUrl, String publishedAt, String sourceName) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.url = url;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
        this.sourceName = sourceName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getSourceName() {
        return sourceName;
    }
}