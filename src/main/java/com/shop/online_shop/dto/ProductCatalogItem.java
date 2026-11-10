package com.shop.online_shop.dto;

import com.shop.online_shop.entity.Product;
import com.shop.online_shop.entity.ProductModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class ProductCatalogItem {

    private final Product product;
    private final ProductModel productModel;

    // Sản phẩm chưa có nhóm
    public ProductCatalogItem(Product product) {
        this.product = product;
        this.productModel = null;
    }

    // Sản phẩm thuộc một nhóm biến thể
    public ProductCatalogItem(ProductModel productModel) {
        this.product = null;
        this.productModel = productModel;
    }

    public Long getId() {
        if (product != null) {
            return product.getId();
        }

        return getVariants().stream()
                .map(Product::getId)
                .filter(id -> id != null)
                .min(Long::compareTo)
                .orElse(null);
    }

    public String getName() {
        return productModel != null
                ? productModel.getName()
                : product.getName();
    }

    public String getDescription() {
        if (productModel != null) {
            return productModel.getDescription();
        }

        return product.getDescription();
    }

    public String getImageUrl() {
        if (productModel == null) {
            return product.getImageUrl();
        }

        if (productModel.getImageUrl() != null
                && !productModel.getImageUrl().isBlank()) {
            return productModel.getImageUrl();
        }

        return getVariants().stream()
                .map(Product::getImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .findFirst()
                .orElse(null);
    }

    public BigDecimal getPrice() {
        if (product != null) {
            return product.getPrice();
        }

        return getVariants().stream()
                .map(Product::getPrice)
                .filter(price -> price != null)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    public Integer getQuantity() {
        if (product != null) {
            return product.getQuantity();
        }

        return getVariants().stream()
                .map(Product::getQuantity)
                .filter(quantity -> quantity != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public LocalDateTime getCreatedAt() {
        if (product != null) {
            return product.getCreatedAt();
        }

        return getVariants().stream()
                .map(Product::getCreatedAt)
                .filter(date -> date != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    public int getVariantCount() {
        return productModel == null ? 1 : getVariants().size();
    }

    public boolean isGrouped() {
        return productModel != null;
    }

    private List<Product> getVariants() {
        if (productModel == null || productModel.getVariants() == null) {
            return List.of();
        }

        return productModel.getVariants().stream()
                .filter(p -> "ACTIVE".equals(p.getStatus()))
                .toList();
    }

}