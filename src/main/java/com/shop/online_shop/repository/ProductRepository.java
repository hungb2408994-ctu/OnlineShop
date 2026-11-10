package com.shop.online_shop.repository;

import com.shop.online_shop.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    // Tìm kiếm theo tên sản phẩm
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // Lọc theo danh mục
    List<Product> findByCategoryId(Long categoryId);

    // Lọc theo khoảng giá
    List<Product> findByPriceBetween(
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    // Tìm kiếm và lọc theo thông tin sản phẩm hoặc nhóm sản phẩm
    @Query("""
        SELECT p FROM Product p
        LEFT JOIN FETCH p.productModel pm
        WHERE (
            :keyword IS NULL
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(pm.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (:categoryId IS NULL OR p.categoryId = :categoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """)
    List<Product> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );


    @Query("""
        SELECT p FROM Product p
        LEFT JOIN FETCH p.productModel pm
        WHERE p.status = 'ACTIVE'
        AND (
                :keyword IS NULL
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(pm.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (:categoryId IS NULL OR p.categoryId = :categoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """)
        List<Product> searchAndFilterActive(
                @Param("keyword") String keyword,
                @Param("categoryId") Long categoryId,
                @Param("minPrice") BigDecimal minPrice,
                @Param("maxPrice") BigDecimal maxPrice
        );

        // Lấy tất cả sản phẩm đang bán
        List<Product> findByStatus(String status);

        // Lấy sản phẩm đang bán theo danh mục
        List<Product> findByCategoryIdAndStatus(
                Long categoryId,
                String status
        );

}