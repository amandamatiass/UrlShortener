package org.acme.shortener.dto;

public class ShortenResponse {

    public String code;
    public String shortUrl;
    public String originalUrl;
    public long createdAt;

    public ShortenResponse() {
    }

    public ShortenResponse(String code, String shortUrl, String originalUrl, long createdAt) {
        this.code = code;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
    }
}
