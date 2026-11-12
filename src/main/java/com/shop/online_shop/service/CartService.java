package com.shop.online_shop.service;

import com.shop.online_shop.entity.Cart;
import com.shop.online_shop.entity.CartItem;
import com.shop.online_shop.entity.Product;
import com.shop.online_shop.entity.User;
import com.shop.online_shop.repository.CartItemRepository;
import com.shop.online_shop.repository.CartRepository;
import com.shop.online_shop.repository.ProductRepository;
import com.shop.online_shop.repository.UserRepository;
import com.shop.online_shop.dto.CartItemResponse;
import com.shop.online_shop.dto.CartResponse;
import com.shop.online_shop.repository.CartProcedureRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;
        private final CartProcedureRepository cartProcedureRepository;

        public CartService(
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        CartProcedureRepository cartProcedureRepository) {

                this.cartRepository = cartRepository;
                this.cartItemRepository = cartItemRepository;
                this.userRepository = userRepository;
                this.productRepository = productRepository;
                this.cartProcedureRepository = cartProcedureRepository;
        }

        @Transactional
        public CartItem addToCart(Long userId, Long productId, Integer quantity) {

                // 1. Validate quantity
                if (quantity == null || quantity <= 0) {
                        throw new RuntimeException("Số lượng phải lớn hơn 0!");
                }

                // 2. Find user
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy người dùng!"));

                // 3. Find product
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy sản phẩm!"));

                // 4. Check product status
                if (!"ACTIVE".equals(product.getStatus())) {
                        throw new RuntimeException(
                                        "Sản phẩm hiện không còn kinh doanh!");
                }

                // 5. Find or create user's cart
                Cart cart = cartRepository.findByUserId(userId)
                                .orElseGet(() -> {
                                        Cart newCart = new Cart();
                                        newCart.setUser(user);
                                        return cartRepository.save(newCart);
                                });

                // 6. Call Stored Procedure
                // MySQL handles stock validation and quantity update.
                cartProcedureRepository.addCartItem(
                                cart.getId(),
                                productId,
                                quantity);

                // 7. Reload CartItem to preserve the original return type
                return cartItemRepository
                                .findByCartIdAndProductId(cart.getId(), productId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không thể thêm sản phẩm vào giỏ hàng!"));
        }

        @Transactional(readOnly = true)
        public CartResponse getCart(Long userId) {

                // 1. Kiểm tra người dùng tồn tại
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy người dùng!"));

                // 2. Tìm giỏ hàng
                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Người dùng chưa có giỏ hàng!"));

                // 3. Gọi Stored Procedure để lấy chi tiết và tổng tiền
                return cartProcedureRepository.getCartResponse(
                                cart.getId(),
                                user.getId());
        }

        @Transactional
        public CartItem updateCartItem(
                        Long userId,
                        Long productId,
                        Integer quantity) {

                // 1. Validate quantity
                if (quantity == null || quantity <= 0) {
                        throw new RuntimeException("Số lượng phải lớn hơn 0!");
                }

                // 2. Find user's cart
                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy giỏ hàng!"));

                // 3. Check whether product exists in cart
                cartItemRepository
                                .findByCartIdAndProductId(cart.getId(), productId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Sản phẩm không có trong giỏ hàng!"));

                // 4. Call Stored Procedure
                cartProcedureRepository.updateCartItem(
                                cart.getId(),
                                productId,
                                quantity);

                // 5. Reload updated CartItem
                return cartItemRepository
                                .findByCartIdAndProductId(cart.getId(), productId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không thể cập nhật sản phẩm trong giỏ hàng!"));
        }

        @Transactional
        public void removeFromCart(Long userId, Long productId) {

                // 1. Find user's cart
                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Không tìm thấy giỏ hàng!"));

                // 2. Check whether product exists in cart
                cartItemRepository
                                .findByCartIdAndProductId(cart.getId(), productId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Sản phẩm không có trong giỏ hàng!"));

                // 3. Call Stored Procedure
                cartProcedureRepository.removeCartItem(
                                cart.getId(),
                                productId);
        }

}