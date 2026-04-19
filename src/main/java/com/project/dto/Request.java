package com.project.dto;

import com.project.util.Method;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Request {
    private Method method;
    private String path;
    private Map<String, String> headers;
    private String body;

    public Request(Method method, String path) {
        this.method = method;
        this.path = path;
        this.headers = new HashMap<>();
        this.body = "";
    }
    public void addHeader(String key, String value) { this.headers.put(key, value); }
    public String getHeader(String key) { return headers.get(key); }
}