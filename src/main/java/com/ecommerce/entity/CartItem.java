package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name= "product_id",nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public BigDecimal getSubtotal(){
        return product.getPrice()
                .multiply(BigDecimal.valueOf(quantity));
    }
}
