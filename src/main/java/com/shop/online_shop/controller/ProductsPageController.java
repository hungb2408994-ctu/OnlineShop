
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        model.addAttribute("categories", categoryRepository.findAll());

        List<ProductCatalogItem> products = new ArrayList<>();
        String filterError = null;

        try {
            // Lấy sản phẩm ACTIVE thỏa mãn điều kiện tìm kiếm và lọc.
            List<Product> result = productService.searchAndFilterActive(
                    keyword,
                    categoryId,
                    minPrice,
                    maxPrice
            );

            if (result != null && !result.isEmpty()) {

                // Lấy các nhóm sản phẩm và biến thể.
                List<ProductModel> allModels =
                        productModelRepository.findAll();

                Map<Long, ProductModel> modelMap = new LinkedHashMap<>();

                for (ProductModel productModel : allModels) {
                    modelMap.put(productModel.getId(), productModel);
                }

                // Ghi nhận các nhóm có ít nhất một sản phẩm phù hợp.
                Set<Long> matchedModelIds = new LinkedHashSet<>();

                for (Product product : result) {

                    if (product.getProductModel() != null) {

                        Long productModelId =
                                product.getProductModel().getId();

                        matchedModelIds.add(productModelId);

                    } else {
                        // Sản phẩm độc lập.
                        products.add(new ProductCatalogItem(product));
                    }
                }

                // Mỗi nhóm chỉ hiển thị một thẻ sản phẩm.
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

        // Sắp xếp danh sách sản phẩm.
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

        // Thông tin tìm kiếm và lọc.
        model.addAttribute("filterError", filterError);

        String selectedCategoryName = null;

        if (categoryId != null) {
            selectedCategoryName = categoryRepository.findById(categoryId)
                    .map(category -> category.getName())
                    .orElse("Danh mục sản phẩm");
        }

        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedCategoryName", selectedCategoryName);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        // Phân trang.
        size = Math.max(1, Math.min(size, 60));

        int totalProducts = products.size();
        int totalPages = (int) Math.ceil((double) totalProducts / size);

        // Đảm bảo page nằm trong phạm vi hợp lệ.
        if (totalPages > 0) {
            page = Math.max(0, Math.min(page, totalPages - 1));
        } else {
            page = 0;
        }

        List<Integer> visiblePages = new ArrayList<>();

        if (totalPages <= 5) {

            for (int i = 0; i < totalPages; i++) {
                visiblePages.add(i);
            }

        } else {

            for (int i = 0; i < 3; i++) {
                visiblePages.add(i);
            }

            int start = Math.max(3, page - 1);
            int end = Math.min(totalPages - 4, page + 1);

            for (int i = start; i <= end; i++) {
                visiblePages.add(i);
            }

            for (int i = totalPages - 3; i < totalPages; i++) {
                visiblePages.add(i);
            }
        }

        model.addAttribute("visiblePages", visiblePages);

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalProducts);

        // Lưu ý: phải là ProductCatalogItem, không phải Product.
        List<ProductCatalogItem> pagedProducts = new ArrayList<>(
                products.subList(fromIndex, toIndex)
        );

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
