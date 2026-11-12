
package com.shop.online_shop.repository;

import com.shop.online_shop.dto.CartItemResponse;
import com.shop.online_shop.dto.CartResponse;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.ConnectionCallback;

import java.sql.CallableStatement;
import java.sql.Connection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CartProcedureRepository {

    private final JdbcTemplate jdbcTemplate;

    public CartProcedureRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CartResponse getCartResponse(Long cartId, Long userId) {

        String sql = """
                CALL sp_get_cart_details(?)
                """;

        return jdbcTemplate.query(
                sql,
                rs -> {

                    List<CartItemResponse> items = new ArrayList<>();

                    BigDecimal totalAmount = BigDecimal.ZERO;

                    while (rs.next()) {

                        totalAmount = rs.getBigDecimal("total_amount");

                        CartItemResponse item = new CartItemResponse(
                                rs.getLong("product_id"),
                                rs.getString("product_name"),
                                rs.getBigDecimal("price"),
                                rs.getInt("quantity"),
                                rs.getBigDecimal("subtotal")
                        );

                        items.add(item);
                    }

                    return new CartResponse(
                            cartId,
                            userId,
                            items,
                            totalAmount
                    );
                },
                cartId
        );
    }

    public void addCartItem(
            Long cartId,
            Long productId,
            Integer quantity) {

        jdbcTemplate.execute(
                (ConnectionCallback<Void>) connection -> {

                    try (CallableStatement statement =
                                connection.prepareCall(
                                        "{CALL sp_add_cart_item(?, ?, ?)}")) {

                        statement.setLong(1, cartId);
                        statement.setLong(2, productId);
                        statement.setInt(3, quantity);

                        statement.execute();
                    }

                    return null;
                }
        );
    }


    public void updateCartItem(Long cartId, Long productId, Integer quantity) {
        jdbcTemplate.execute(
            (ConnectionCallback<Void>) connection -> {
                try (CallableStatement statement =
                        connection.prepareCall("{CALL sp_update_cart_item(?, ?, ?)}")) {

                    statement.setLong(1, cartId);
                    statement.setLong(2, productId);
                    statement.setInt(3, quantity);

                    statement.execute();
                }

                return null;
            }
        );
    }


    public void removeCartItem(Long cartId, Long productId) {
        jdbcTemplate.execute(
            (ConnectionCallback<Void>) connection -> {
                try (CallableStatement statement =
                        connection.prepareCall("{CALL sp_remove_cart_item(?, ?)}")) {

                    statement.setLong(1, cartId);
                    statement.setLong(2, productId);

                    statement.execute();
                }

                return null;
            }
        );
    }

}
