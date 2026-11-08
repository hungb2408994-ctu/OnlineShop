
package com.shop.online_shop.repository;

import com.shop.online_shop.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

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

    // Tìm kiếm kết hợp nhiều điều kiện
    @Query("""
        SELECT p FROM Product p
        WHERE (:keyword IS NULL OR
               LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
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
    // Lấy danh sách sản phẩm bán chạy nhất
        @Query("""
        SELECT oi.product
        FROM OrderItem oi
        JOIN oi.order o
        WHERE UPPER(o.status) <> 'CANCELLED'
        GROUP BY oi.product
        ORDER BY SUM(oi.quantity) DESC, oi.product.id ASC
        """)
        List<Product> findBestSellingProducts(Pageable pageable);

        //Tìm kiếm và lọc sản phẩm bán chạy nhất theo điều kiện:
        // Lấy sản phẩm có tổng số lượng bán cao nhất lên đầu
        // Sản phẩm chưa từng xuất hiện trong đơn hàng sẽ không xuất hiện trong danh sách bán chạy.
        @Query("""
        SELECT p
        FROM Product p
        JOIN OrderItem oi ON oi.product = p
        JOIN oi.order o
        WHERE o.status <> 'CANCELLED'
        AND (:keyword IS NULL OR
                LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:categoryId IS NULL OR p.categoryId = :categoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        GROUP BY p
        ORDER BY SUM(oi.quantity) DESC, p.id ASC
        """)
        List<Product> searchAndFilterByBestSelling(
                @Param("keyword") String keyword,
                @Param("categoryId") Long categoryId,
                @Param("minPrice") BigDecimal minPrice,
                @Param("maxPrice") BigDecimal maxPrice
        );

}
