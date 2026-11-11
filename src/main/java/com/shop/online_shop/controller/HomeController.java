
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final ProductModelRepository productModelRepository;

    public HomeController(
            ProductService productService,
            CategoryRepository categoryRepository,
            ProductModelRepository productModelRepository) {

        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.productModelRepository = productModelRepository;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) Long categoryId,
            Model model) {

        // 1. Lấy danh sách danh mục
        model.addAttribute(
                "categories",
                categoryRepository.findAll()
        );

        // 2. Lấy sản phẩm theo danh mục hoặc toàn bộ sản phẩm
        List<Product> result;

        if (categoryId != null) {
            result = productService.getActiveProductsByCategory(categoryId);
        } else {
            result = productService.getAllActiveProducts();
        }

        // 3. Chuẩn bị danh sách sản phẩm hiển thị
        List<ProductCatalogItem> products = new ArrayList<>();

        if (result != null && !result.isEmpty()) {

            // Lấy tất cả nhóm sản phẩm cùng các phiên bản
            List<ProductModel> allModels =
                    productModelRepository.findAll();

            Map<Long, ProductModel> modelMap = new LinkedHashMap<>();

            for (ProductModel productModel : allModels) {
                modelMap.put(productModel.getId(), productModel);
            }

            // Ghi nhận những nhóm có ít nhất một phiên bản phù hợp
            Set<Long> matchedModelIds = new HashSet<>();

            // Thêm sản phẩm độc lập và ghi nhận nhóm biến thể
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

        // 4. Truyền danh sách đã gom nhóm sang trang chủ
        model.addAttribute("products", products);

        // 5. Tạo danh sách sản phẩm nổi bật cho Hero
        List<ProductCatalogItem> heroProducts =
                new ArrayList<>(products);

        Collections.shuffle(heroProducts);

        heroProducts = new ArrayList<>(
                heroProducts.subList(
                        0,
                        Math.min(3, heroProducts.size())
                )
        );

        model.addAttribute("heroProducts", heroProducts);

        // 6. Lưu danh mục đang được chọn
        model.addAttribute("selectedCategoryId", categoryId);

        return "user/index";
    }
}
