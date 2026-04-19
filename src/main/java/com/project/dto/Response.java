package com.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Response {
    private int statusCode;
    private String statusText;
    private Map<String, String> headers;
    private String body;

    public Response() {
        this.statusCode = 200;
        this.statusText = "OK";
        this.headers = new HashMap<>();
        this.body = "";

        this.headers.put("Content-Type", "application/json");
    }

    public void addHeader(String key, String value) { this.headers.put(key, value); }

    public void setBody(String body) {
        this.body = body;
        this.headers.put("Content-Length", String.valueOf(body.getBytes().length));
    }

    @Override
    public String toString() {
        return statusCode + " " + statusText + "\r\n" +
                headers.entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .reduce((s1, s2) -> s1 + "\r\n" + s2)
                        .orElse("") + "\r\n\r\n" + body;
    }
}