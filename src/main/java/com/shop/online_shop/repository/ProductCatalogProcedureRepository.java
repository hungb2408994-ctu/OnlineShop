
package com.shop.online_shop.repository;

import com.shop.online_shop.entity.Product;
import com.shop.online_shop.entity.ProductModel;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class ProductCatalogProcedureRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductCatalogProcedureRepository(
            JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> searchActiveProducts(
            String keyword,
            Long categoryId,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice) {

        String sql = """
                CALL sp_search_active_products(?, ?, ?, ?)
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {

                    Product product = new Product();

                    product.setId(
                            rs.getLong("product_id")
                    );

                    product.setName(
                            rs.getString("product_name")
                    );

                    product.setDescription(
                            rs.getString("description")
                    );

                    product.setPrice(
                            rs.getBigDecimal("price")
                    );

                    product.setQuantity(
                            rs.getInt("stock_quantity")
                    );

                    product.setImageUrl(
                            rs.getString("image_url")
                    );

                    product.setCategoryId(
                            rs.getLong("category_id")
                    );

                    Timestamp createdAt =
                            rs.getTimestamp("created_at");

                    if (createdAt != null) {
                        product.setCreatedAt(
                                createdAt.toLocalDateTime()
                        );
                    }

                    product.setVariantName(
                            rs.getString("variant_name")
                    );

                    product.setStatus(
                            rs.getString("status")
                    );

                    // Gắn ProductModel theo ID.
                    // ProductModelRepository sẽ tải model
                    // cùng toàn bộ variants ở Controller.
                    long productModelId =
                            rs.getLong("product_model_id");

                    if (!rs.wasNull()) {

                        ProductModel productModel =
                                new ProductModel();

                        productModel.setId(productModelId);

                        product.setProductModel(productModel);
                    }

                    return product;
                },
                keyword,
                categoryId,
                minPrice,
                maxPrice
        );
    }
}
