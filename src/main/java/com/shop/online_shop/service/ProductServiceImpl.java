
package com.shop.online_shop.service;

import com.shop.online_shop.entity.Product;
import com.shop.online_shop.repository.ProductRepository;
import com.shop.online_shop.repository.ProductCatalogProcedureRepository;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCatalogProcedureRepository productCatalogProcedureRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductCatalogProcedureRepository productCatalogProcedureRepository) {
        this.productRepository = productRepository;
        this.productCatalogProcedureRepository = productCatalogProcedureRepository;
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

        validatePriceRange(minPrice, maxPrice);
        keyword = normalizeKeyword(keyword);

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

    // Lấy tất cả sản phẩm đang bán.
    @Override
    public List<Product> getAllActiveProducts() {
        return productRepository.findByStatus("ACTIVE");
    }

    // Lấy sản phẩm đang bán theo danh mục.
    @Override
    public List<Product> getActiveProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndStatus(
                categoryId,
                "ACTIVE"
        );
    }

    // Tìm kiếm và lọc sản phẩm đang bán.
    @Override
    public List<Product> searchAndFilterActive(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        validatePriceRange(minPrice, maxPrice);
        keyword = normalizeKeyword(keyword);

        return productCatalogProcedureRepository.searchActiveProducts(
                keyword,
                categoryId,
                minPrice,
                maxPrice
        );
    }

    // Lấy danh sách sản phẩm bán chạy nhất theo số lượng giới hạn.
    @Override
    public List<Product> getBestSellingProducts(int limit) {

        if (limit <= 0) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(0, limit);

        return productRepository.findBestSellingProducts(pageable);
    }

    // Tìm kiếm và lọc sản phẩm theo số lượng bán.
    @Override
    public List<Product> getBestSellingProducts(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        validatePriceRange(minPrice, maxPrice);
        keyword = normalizeKeyword(keyword);

        return productRepository.searchAndFilterByBestSelling(
                keyword,
                categoryId,
                minPrice,
                maxPrice
        );
    }

    // Chuẩn hóa từ khóa tìm kiếm.
    private String normalizeKeyword(String keyword) {

        if (keyword != null) {
            keyword = keyword.trim();

            if (keyword.isEmpty()) {
                keyword = null;
            }
        }

        return keyword;
    }

    // Kiểm tra điều kiện lọc giá.
    private void validatePriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        if (minPrice != null
                && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Giá tối thiểu không được nhỏ hơn 0."
            );
        }

        if (maxPrice != null
                && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Giá tối đa không được nhỏ hơn 0."
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    "Giá tối thiểu không được lớn hơn giá tối đa."
            );
        }
    }
}
