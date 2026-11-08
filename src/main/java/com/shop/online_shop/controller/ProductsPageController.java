package com.shop.online_shop.controller;

import com.shop.online_shop.entity.Product;
import com.shop.online_shop.repository.CategoryRepository;
import com.shop.online_shop.service.ProductService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class ProductsPageController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductsPageController(
            ProductService productService,
            CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        // Lấy danh sách danh mục cho sidebar
        model.addAttribute("categories", categoryRepository.findAll());

        // Lấy 8 sản phẩm bán chạy nhất
        model.addAttribute(
                "bestSellingProducts",
                productService.getBestSellingProducts(8)
        );

        // Khởi tạo danh sách sản phẩm rỗng
        List<Product> products = new ArrayList<>();
        String filterError = null;

        try {
            List<Product> result;

            if ("best-selling".equals(sort)) {
                result = productService.getBestSellingProducts(
                        keyword,
                        categoryId,
                        minPrice,
                        maxPrice
                );
            } else {
                result = productService.searchAndFilter(
                        keyword,
                        categoryId,
                        minPrice,
                        maxPrice
                );
            }

            // Chỉ thêm sản phẩm nếu kết quả không null
            if (result != null) {
                products.addAll(result);
            }

        } catch (IllegalArgumentException e) {
            filterError = e.getMessage();
        }

        // Sắp xếp theo giá
        if ("price-asc".equals(sort)) {
            products.sort(Comparator.comparing(Product::getPrice));
        } else if ("price-desc".equals(sort)) {
            products.sort(
                    Comparator.comparing(Product::getPrice).reversed()
            );
        } else if ("newest".equals(sort)) {
            products.sort(
                    Comparator.comparing(
                            Product::getCreatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );
        }
        // best-selling đã được sắp xếp trong câu truy vấn SQL


        // Truyền dữ liệu sang Thymeleaf
        model.addAttribute("products", products);
        model.addAttribute("filterError", filterError);

        // Thông tin danh mục đang chọn
        String selectedCategoryName = null;

        if (categoryId != null) {
            selectedCategoryName = categoryRepository.findById(categoryId)
                    .map(category -> category.getName())
                    .orElse("Danh mục sản phẩm");
        }

        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedCategoryName", selectedCategoryName);

        // Giữ lại điều kiện tìm kiếm và sắp xếp
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        // Giới hạn số sản phẩm mỗi trang
        size = Math.max(1, Math.min(size, 60));

        // Tính tổng số sản phẩm và số trang
        int totalProducts = products.size();
        int totalPages = (int) Math.ceil((double) totalProducts / size);

        // Xác định vùng trang ở giữa, quanh trang hiện tại
        // Tính vùng trang ở giữa, tránh trùng với 3 trang đầu và cuối
        int startPage = Math.max(3, page - 1);
        int endPage = Math.min(totalPages - 4, page + 1);

        // Tạo danh sách số trang cần hiển thị (đánh số từ 0)
        List<Integer> visiblePages = new ArrayList<>();

        if (totalPages <= 5) {
            // Tối đa 5 trang: hiển thị tất cả
            for (int i = 0; i < totalPages; i++) {
                visiblePages.add(i);
            }
        } else {
            // 3 trang đầu
            for (int i = 0; i < 3; i++) {
                visiblePages.add(i);
            }

            // Các trang quanh trang hiện tại
            int start = Math.max(3, page - 1);
            int end = Math.min(totalPages - 4, page + 1);

            for (int i = start; i <= end; i++) {
                visiblePages.add(i);
            }

            // 3 trang cuối
            for (int i = totalPages - 3; i < totalPages; i++) {
                visiblePages.add(i);
            }
        }
        model.addAttribute("visiblePages", visiblePages);

        // Đảm bảo page không nằm ngoài phạm vi
        if (totalPages > 0) {
            page = Math.max(0, Math.min(page, totalPages - 1));
        } else {
            page = 0;
        }

        // Xác định vị trí sản phẩm cần lấy
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalProducts);

        // Lấy sản phẩm thuộc trang hiện tại
        List<Product> pagedProducts = new ArrayList<>(
                products.subList(fromIndex, toIndex)
        );

        // Đưa dữ liệu lên giao diện
        model.addAttribute("products", pagedProducts);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("hasNext", page < totalPages - 1);

        return "user/products";
    }
}