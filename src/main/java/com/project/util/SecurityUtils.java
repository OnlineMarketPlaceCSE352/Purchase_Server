package com.project.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.Base64;

public class SecurityUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getRoleFromToken(String token) {
        return extractField(token, "role", "USER");
    }

    public static String getUserIdFromToken(String token) {
        return extractField(token, "sub", "unknown_user");
    }

    private static String extractField(String token, String fieldName, String defaultValue) {
        try {
            String actualToken = token.replace("Bearer ", "").trim();
            String[] chunks = actualToken.split("\\.");
            if (chunks.length < 2) return defaultValue;

            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payloadJson = new String(decoder.decode(chunks[1]));

            JsonNode payloadNode = mapper.readTree(payloadJson);

            if (payloadNode.has(fieldName)) {
                return payloadNode.get(fieldName).asString();
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
