package com.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(
            name="user_id",
            nullable = false,
            unique = true
    )
    @JsonIgnore
    private User user;
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true,fetch = FetchType.EAGER)
    @JoinColumn(name="cart_id")
    @Builder.Default
    private List<CartItem> items=new ArrayList<>();

    public BigDecimal getTotalAmount(){
        return items.stream().map(CartItem::getSubtotal).reduce(
                BigDecimal.ZERO,
                BigDecimal::add
        );
    }
}


