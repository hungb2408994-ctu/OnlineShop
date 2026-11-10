package com.shop.online_shop.controller;

import com.shop.online_shop.dto.ProductCatalogItem;
import com.shop.online_shop.entity.Product;
import com.shop.online_shop.entity.ProductModel;
import com.shop.online_shop.repository.CategoryRepository;
import com.shop.online_shop.repository.ProductModelRepository;
import com.shop.online_shop.service.ProductService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class ProductsPageController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final ProductModelRepository productModelRepository;

    public ProductsPageController(
            ProductService productService,
            CategoryRepository categoryRepository,
            ProductModelRepository productModelRepository) {

        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.productModelRepository = productModelRepository;
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            Model model) {

        // Lấy danh sách danh mục cho sidebar
        model.addAttribute("categories", categoryRepository.findAll());

        List<ProductCatalogItem> products = new ArrayList<>();
        String filterError = null;

        try {
            // Tìm những Product thỏa mãn điều kiện tìm kiếm và lọc
            List<Product> result = productService.searchAndFilterActive(
                    keyword,
                    categoryId,
                    minPrice,
                    maxPrice
            );
System.out.println("========== PRODUCT GROUP DEBUG ==========");

for (Product p : result) {
    System.out.println(
            "Product ID: " + p.getId()
            + " | Name: " + p.getName()
            + " | Model ID: "
            + (p.getProductModel() != null
                ? p.getProductModel().getId()
                : "NULL")
    );
}

System.out.println("==========================================");
            if (result != null && !result.isEmpty()) {

                // Lấy toàn bộ ProductModel cùng các biến thể
                List<ProductModel> allModels =
                        productModelRepository.findAll();

                Map<Long, ProductModel> modelMap = new LinkedHashMap<>();

                for (ProductModel productModel : allModels) {
                    modelMap.put(productModel.getId(), productModel);
                }

                // Ghi nhận các nhóm có ít nhất một biến thể phù hợp
                Set<Long> matchedModelIds = new HashSet<>();

                // Sản phẩm chưa thuộc nhóm
                for (Product product : result) {

                    if (product.getProductModel() != null) {

                        Long productModelId =
                                product.getProductModel().getId();

                        matchedModelIds.add(productModelId);

                    } else {

                        products.add(new ProductCatalogItem(product));
                    }
                }

                // Mỗi nhóm chỉ tạo một thẻ sản phẩm
                for (Long productModelId : matchedModelIds) {

                    ProductModel productModel =
                            modelMap.get(productModelId);

                    if (productModel != null
                            && productModel.getVariants() != null
                            && !productModel.getVariants().isEmpty()) {

                        products.add(
                                new ProductCatalogItem(productModel)
                        );
                    }
                }
            }

        } catch (IllegalArgumentException e) {
            filterError = e.getMessage();
        }

        // Sắp xếp danh sách sản phẩm
        System.out.println("========== FINAL CATALOG DEBUG ==========");

for (ProductCatalogItem item : products) {
    System.out.println(
            "ID: " + item.getId()
            + " | Name: " + item.getName()
            + " | Grouped: " + item.isGrouped()
            + " | Variant count: " + item.getVariantCount()
    );
}

System.out.println("Total catalog cards: " + products.size());
System.out.println("=========================================");
        if ("price-asc".equals(sort)) {

            products.sort(
                    Comparator.comparing(
                            ProductCatalogItem::getPrice,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    )
            );

        } else if ("price-desc".equals(sort)) {

            products.sort(
                    Comparator.comparing(
                            ProductCatalogItem::getPrice,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );

        } else if ("newest".equals(sort)) {

            products.sort(
                    Comparator.comparing(
                            ProductCatalogItem::getCreatedAt,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    )
            );
        }

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

        return "user/products";
    }
}