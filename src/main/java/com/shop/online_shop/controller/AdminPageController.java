package com.shop.online_shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin/products")
    public String showProductsPage() {
        return "admin/products";
    }

    @GetMapping("/admin/categories")
    public String showCategoriesPage() {
        return "admin/categories";
    }

    @GetMapping("/admin/orders")
    public String showOrdersPage() {
        return "admin/orders";
    }
}