package com.project.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class Purchase {
    private String id;
    private String sellerID;
    private String buyerID;
    private String productID;
    private Date date;
    private BigDecimal price;
}
