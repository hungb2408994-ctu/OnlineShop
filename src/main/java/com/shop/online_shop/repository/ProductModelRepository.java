
package com.shop.online_shop.repository;

import com.shop.online_shop.entity.ProductModel;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductModelRepository
        extends JpaRepository<ProductModel, Long> {

    // Lấy danh sách sản phẩm cha cùng các phiên bản
    @EntityGraph(attributePaths = {"variants"})
    List<ProductModel> findAll();

}
