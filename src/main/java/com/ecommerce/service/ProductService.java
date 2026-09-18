package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {


    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    public ProductService(ProductRepository productRepository, CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public Product createProduct(
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            List<MultipartFile> images
    ) {
        validateProductDetails(name, price, stock);
        validateImages(images, true);
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            String imageUrl = cloudinaryService.uploadImage(image);
            imageUrls.add(imageUrl);
        }
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .imageUrls(imageUrls)
                .build();
        return productRepository.save(product);

    }

    private void validateImages(List<MultipartFile> images, boolean required) {
        if (required && (images == null || images.isEmpty())) {
            throw new IllegalArgumentException("At least one product image is required");
        }
        if (images != null) {
            for (MultipartFile image : images) {
                if (image.isEmpty()) {
                    throw new IllegalArgumentException("image is empty");
                }
                String fileName = image.getOriginalFilename();
                if (fileName == null) {
                    throw new IllegalArgumentException("image filename is empty");
                }
                String lowerFileName = fileName.toLowerCase();
                if (!(lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") || lowerFileName.endsWith(".webp") || lowerFileName.endsWith(".png") || lowerFileName.endsWith(".gif"))) {
                    throw new IllegalArgumentException("Only jpg, jpeg, webp or .png are supported");
                }
            }
        }
    }

    private void validateProductDetails(String name, BigDecimal price, Integer stock) {
        if (name == null || name.isEmpty() || price == null || price.compareTo(BigDecimal.ZERO) <= 0 || stock == null || stock <= 0) {
            throw new IllegalArgumentException("Product name or price cannot be null or empty or negative");
        }
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    public void deleteProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        productRepository.delete(product);
        System.out.println(" Product Deleted Successfully");
    }

    public Product updateProduct(Long id, String name, String description, BigDecimal price, Integer stock, List<MultipartFile> images) {
        validateImages(images, false);
        validateProductDetails(name, price, stock);
        Product product = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                String imageUrl = cloudinaryService.uploadImage(image);
                imageUrls.add(imageUrl);
            }
            product.setImageUrls(imageUrls);
        }

        return productRepository.save(product);
    }

    public Page<Product> getAllProducts(String keyword, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page cannot be negative");
        }
        if (size < 0) {
            throw new IllegalArgumentException("size cannot be negative");
        }
        Pageable pageable = PageRequest.of(page, size);

        if (keyword == null || keyword.isBlank()) {
            return productRepository.findAll(pageable);

        }
        return productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
    }

}
