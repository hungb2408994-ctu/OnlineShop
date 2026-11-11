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
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {

        // 1. Kiểm tra số lượng hợp lệ
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0!");
        }

        // 2. Tìm người dùng
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 3. Tìm sản phẩm
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        // Không cho phép thêm sản phẩm đã bị ẩn
        if (!"ACTIVE".equals(product.getStatus())) {
        throw new RuntimeException("Sản phẩm hiện không còn kinh doanh!");
        }

        // 4. Kiểm tra tồn kho
        if (product.getQuantity() == null || product.getQuantity() < quantity) {
            throw new RuntimeException("Số lượng sản phẩm trong kho không đủ!");
        }

        // 5. Tìm giỏ hàng của người dùng
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // 6. Kiểm tra sản phẩm đã có trong giỏ hàng chưa
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (cartItem != null) {

            // Nếu đã có thì cộng thêm số lượng
            int newQuantity = cartItem.getQuantity() + quantity;

            if (newQuantity > product.getQuantity()) {
                throw new RuntimeException("Số lượng vượt quá tồn kho!");
            }

            cartItem.setQuantity(newQuantity);

        } else {

            // Nếu chưa có thì tạo CartItem mới
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
        }

        return cartItemRepository.save(cartItem);
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

        // 3. Chuyển CartItem thành CartItemResponse
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(item -> {

                    Product product = item.getProduct();

                    BigDecimal subtotal = product.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

                    return new CartItemResponse(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            item.getQuantity(),
                            subtotal
                    );
                })
                .collect(Collectors.toList());

        // 4. Tính tổng tiền
        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Trả về kết quả
        return new CartResponse(
                cart.getId(),
                user.getId(),
                items,
                totalAmount
        );
    }
    @Transactional
    public CartItem updateCartItem(
            Long userId,
            Long productId,
            Integer quantity) {

        // 1. Kiểm tra số lượng
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0!");
        }

        // 2. Tìm giỏ hàng
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy giỏ hàng!"));

        // 3. Tìm sản phẩm trong giỏ hàng
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException(
                        "Sản phẩm không có trong giỏ hàng!"));

        // 4. Kiểm tra tồn kho
        Product product = cartItem.getProduct();

        // Không cho phép cập nhật sản phẩm đã bị ẩn
        if (!"ACTIVE".equals(product.getStatus())) {
                throw new RuntimeException("Sản phẩm hiện không còn kinh doanh!");
        }

        if (product.getQuantity() == null ||
                quantity > product.getQuantity()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho!");
        }

        // 5. Cập nhật số lượng
        cartItem.setQuantity(quantity);

        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {

        // 1. Tìm giỏ hàng của người dùng
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy giỏ hàng!"));

        // 2. Tìm sản phẩm trong giỏ hàng
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException(
                        "Sản phẩm không có trong giỏ hàng!"));

        // 3. Xóa sản phẩm khỏi giỏ hàng
        cartItemRepository.delete(cartItem);
    }
}