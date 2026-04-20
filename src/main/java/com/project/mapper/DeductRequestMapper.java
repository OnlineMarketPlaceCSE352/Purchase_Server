package com.project.mapper;

import com.project.dto.DeductRequest;
import tools.jackson.databind.ObjectMapper;

public class DeductRequestMapper {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String mapToJSON(DeductRequest request) {
        try {
            return mapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize DeductRequest to JSON");
        }
    }
}
