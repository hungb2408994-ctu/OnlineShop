
package com.shop.online_shop.service;

import com.shop.online_shop.entity.Product;
import com.shop.online_shop.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("Không tìm thấy sản phẩm ID: " + id)
                );
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> searchAndFilter(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        // Chuẩn hóa từ khóa
        if (keyword != null) {
            keyword = keyword.trim();

            if (keyword.isEmpty()) {
                keyword = null;
            }
        }

        // Kiểm tra giá âm
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Giá tối thiểu không được nhỏ hơn 0."
            );
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Giá tối đa không được nhỏ hơn 0."
            );
        }

        // Kiểm tra khoảng giá
        if (minPrice != null && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    "Giá tối thiểu không được lớn hơn giá tối đa."
            );
        }

        return productRepository.searchAndFilter(
                keyword,
                categoryId,
                minPrice,
                maxPrice
        );
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository
                .findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<Product> filterProducts(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return productRepository.findByPriceBetween(
                minPrice,
                maxPrice
        );
    }

    @Override
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product) {

        Product existingProduct = getProductById(id);

        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setImageUrl(product.getImageUrl());

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
