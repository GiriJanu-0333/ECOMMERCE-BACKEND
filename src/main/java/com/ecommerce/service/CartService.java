package com.ecommerce.service;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository cartRepository;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;


    public CartService(
            CartRepository cartRepository,
            UserRepository userRepository,
            ProductRepository productRepository
    ) {

        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }


    // ===============================
    // GET CURRENT USER CART
    // ===============================

    public Cart getCart(String username) {

        User user = getUserByUsername(username);

        return getOrCreateCart(user);
    }


    // ===============================
    // ADD PRODUCT TO CART
    // ===============================

    public Cart addToCart(
            String username,
            Long productId,
            Integer quantity
    ) {

        validateQuantity(quantity);


        User user =
                getUserByUsername(username);


        Product product =
                getProductById(productId);


        Cart cart =
                getOrCreateCart(user);


        CartItem existingItem =
                cart.getItems()
                        .stream()
                        .filter(item ->
                                item.getProduct()
                                        .getId()
                                        .equals(productId)
                        )
                        .findFirst()
                        .orElse(null);


        if (existingItem != null) {

            int newQuantity =
                    existingItem.getQuantity()
                            + quantity;


            validateStock(
                    product,
                    newQuantity
            );


            existingItem.setQuantity(
                    newQuantity
            );

        } else {

            validateStock(
                    product,
                    quantity
            );


            CartItem cartItem =
                    CartItem.builder()
                            .product(product)
                            .quantity(quantity)
                            .build();


            cart.getItems()
                    .add(cartItem);
        }


        return cartRepository.save(cart);
    }


    // ===============================
    // UPDATE PRODUCT QUANTITY
    // ===============================

    public Cart updateQuantity(
            String username,
            Long productId,
            Integer quantity
    ) {

        validateQuantity(quantity);


        User user =
                getUserByUsername(username);


        Cart cart =
                getOrCreateCart(user);


        CartItem cartItem =
                cart.getItems()
                        .stream()
                        .filter(item ->
                                item.getProduct()
                                        .getId()
                                        .equals(productId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product is not present in cart"
                                )
                        );


        Product product =
                cartItem.getProduct();


        validateStock(
                product,
                quantity
        );


        cartItem.setQuantity(quantity);


        return cartRepository.save(cart);
    }


    // ===============================
    // REMOVE PRODUCT FROM CART
    // ===============================

    public Cart removeFromCart(
            String username,
            Long productId
    ) {

        User user =
                getUserByUsername(username);


        Cart cart =
                getOrCreateCart(user);


        boolean removed =
                cart.getItems()
                        .removeIf(item ->
                                item.getProduct()
                                        .getId()
                                        .equals(productId)
                        );


        if (!removed) {

            throw new IllegalArgumentException(
                    "Product is not present in cart"
            );
        }


        return cartRepository.save(cart);
    }


    // ===============================
    // CLEAR CART
    // ===============================

    public Cart clearCart(String username) {

        User user =
                getUserByUsername(username);


        Cart cart =
                getOrCreateCart(user);


        cart.getItems().clear();


        return cartRepository.save(cart);
    }


    // ===============================
    // GET USER
    // ===============================

    private User getUserByUsername(
            String username
    ) {

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }


    // ===============================
    // GET PRODUCT
    // ===============================

    private Product getProductById(
            Long productId
    ) {

        return productRepository
                .findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found"
                        )
                );
    }


    // ===============================
    // FIND OR CREATE CART
    // ===============================

    private Cart getOrCreateCart(
            User user
    ) {

        return cartRepository
                .findByUser(user)
                .orElseGet(() -> {

                    Cart cart =
                            Cart.builder()
                                    .user(user)
                                    .build();


                    return cartRepository.save(
                            cart
                    );
                });
    }


    // ===============================
    // QUANTITY VALIDATION
    // ===============================

    private void validateQuantity(
            Integer quantity
    ) {

        if (quantity == null ||
                quantity <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }
    }


    // ===============================
    // STOCK VALIDATION
    // ===============================

    private void validateStock(
            Product product,
            Integer quantity
    ) {

        if (product.getStock() <= 0) {

            throw new IllegalArgumentException(
                    "Product is out of stock"
            );
        }


        if (quantity > product.getStock()) {

            throw new IllegalArgumentException(
                    "Requested quantity exceeds available stock"
            );
        }
    }
}