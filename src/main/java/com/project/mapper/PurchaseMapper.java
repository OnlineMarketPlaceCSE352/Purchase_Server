package com.project.mapper;

import com.project.dto.PurchaseRequest;
import com.project.model.Purchase;
import tools.jackson.databind.ObjectMapper;

public class PurchaseMapper {
    private static ObjectMapper mapper = new ObjectMapper();

    public static String mapToJSON(Purchase purchase) {
        try {
            return mapper.writeValueAsString(purchase);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static PurchaseRequest mapToPurchaseRequest(String json) {
        try {
            return mapper.readValue(json, PurchaseRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON payload");
        }
    }
}
