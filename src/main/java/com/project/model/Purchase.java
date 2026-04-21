package com.project.model;



import jakarta.persistence.*;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;


@Entity
@Table(name = "purchases")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {
    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "seller_id", nullable = false)
    private String sellerID;

    @Column(name = "buyer_id", nullable = false)
    private String buyerID;

    @Column(name = "product_id", nullable = false)
    private String productID;

    @Column(name = "purchase_date")
    private Date date;

    @Column(name = "price", precision = 18, scale = 2)
    private BigDecimal price;

    @PrePersist
    public void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
