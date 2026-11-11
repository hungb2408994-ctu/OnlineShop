package com.shop.online_shop.controller;

import com.shop.online_shop.entity.Product;
import com.shop.online_shop.entity.ProductModel;
import com.shop.online_shop.repository.ProductModelRepository;
import com.shop.online_shop.service.ProductService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ProductDetailController {

    private final ProductService productService;
    private final ProductModelRepository productModelRepository;

    public ProductDetailController(
            ProductService productService,
            ProductModelRepository productModelRepository) {
        this.productService = productService;
        this.productModelRepository = productModelRepository;
    }

    @GetMapping("/products/{id}")
    public String productDetail(
            @PathVariable Long id,
            Model model) {

        Product product;

        try {
            product = productService.getProductById(id);
        } catch (RuntimeException e) {
            return "redirect:/products";
        }

        // Không cho mở trang chi tiết sản phẩm đã ngừng bán
        if (!"ACTIVE".equals(product.getStatus())) {
            return "redirect:/products";
        }

        List<Product> variants;
        ProductModel productModel = null;

        if (product.getProductModel() != null
                && product.getProductModel().getId() != null) {

            Long productModelId = product.getProductModel().getId();

            productModel = productModelRepository
                    .findWithVariantsById(productModelId)
                    .orElse(null);
        }

        if (productModel != null && productModel.getVariants() != null) {

            variants = productModel.getVariants().stream()
                    .filter(p -> "ACTIVE".equals(p.getStatus()))
                    .toList();

        } else {
            variants = List.of(product);
        }

        model.addAttribute("product", product);
        model.addAttribute("productModel", productModel);
        model.addAttribute("variants", variants);

        return "user/product-detail";
    }
}