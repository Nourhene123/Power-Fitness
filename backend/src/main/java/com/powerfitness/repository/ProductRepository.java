package com.powerfitness.repository;

import com.powerfitness.entity.Product;
import com.powerfitness.entity.ProductCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    List<Product> findByActiveTrueAndCategoryOrderByCreatedAtDesc(ProductCategory category);

    List<Product> findAllByOrderByCreatedAtDesc();
}
