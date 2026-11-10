package com.shop.online_shop.service;

import java.math.BigDecimal;
import java.util.List;

import com.shop.online_shop.entity.Product;

public interface ProductService {

    // Lấy danh sách sản phẩm
    List<Product> getAllProducts();

    // Lấy sản phẩm theo ID
    Product getProductById(Long id);

    // Lọc sản phẩm theo danh mục
    List<Product> getProductsByCategory(Long categoryId);

    // Tìm kiếm sản phẩm theo tên
    List<Product> searchProducts(String keyword);

    // Lọc sản phẩm theo khoảng giá
    List<Product> filterProducts(BigDecimal minPrice, BigDecimal maxPrice);

    // Thêm sản phẩm
    Product addProduct(Product product);

    // Cập nhật sản phẩm
    Product updateProduct(Long id, Product product);

    // Xóa sản phẩm
    void deleteProduct(Long id);

    // Tìm kiếm và lọc sản phẩm theo nhiều điều kiện
    List<Product> searchAndFilter(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    // Lấy tất cả sản phẩm đang bán
    List<Product> getAllActiveProducts();

    // Lấy sản phẩm đang bán theo danh mục
    List<Product> getActiveProductsByCategory(Long categoryId);

    // Tìm kiếm và lọc sản phẩm đang bán
    List<Product> searchAndFilterActive(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

}