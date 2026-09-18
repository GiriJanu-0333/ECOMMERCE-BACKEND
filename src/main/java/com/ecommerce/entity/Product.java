package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(length = 2000)
    private String description;
    @Column(nullable = false)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer stock;

    @ElementCollection
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
}
