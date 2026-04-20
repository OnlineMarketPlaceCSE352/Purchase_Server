package com.project.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductDTO {
    private String id;
    private String sellerID;
    private BigDecimal price;
}