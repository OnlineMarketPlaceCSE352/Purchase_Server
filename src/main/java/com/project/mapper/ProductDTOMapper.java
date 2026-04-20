package com.project.mapper;

import com.project.dto.ProductDTO;
import tools.jackson.databind.ObjectMapper;

public class ProductDTOMapper {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ProductDTO mapToProductDTO(String json) {
        try {
            return mapper.readValue(json, ProductDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Product JSON from microservice");
        }
    }
}
