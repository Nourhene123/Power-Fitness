package com.powerfitness.Repository;

import com.powerfitness.Entity.Product;
import com.powerfitness.Entity.ProductCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    List<Product> findByActiveTrueAndCategoryOrderByCreatedAtDesc(ProductCategory category);

    List<Product> findAllByOrderByCreatedAtDesc();
}
