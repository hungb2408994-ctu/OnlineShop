package com.shop.online_shop.repository;

import com.shop.online_shop.entity.ProductModel;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductModelRepository
        extends JpaRepository<ProductModel, Long> {

    // Lấy danh sách sản phẩm cha cùng các phiên bản
    @EntityGraph(attributePaths = {"variants"})
    List<ProductModel> findAll();

    // Lấy một sản phẩm cha cùng toàn bộ phiên bản
    @EntityGraph(attributePaths = {"variants"})
    Optional<ProductModel> findWithVariantsById(Long id);
}