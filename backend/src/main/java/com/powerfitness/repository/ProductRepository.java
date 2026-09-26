package com.powerfitness.repository;

import com.powerfitness.entity.Product;
import com.powerfitness.entity.ProductCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByActiveTrueOrderByCreatedAtDesc();

    List<Product> findByActiveTrueAndCategoryOrderByCreatedAtDesc(ProductCategory category);

    List<Product> findAllByOrderByCreatedAtDesc();

    /**
     * Atomically takes {@code quantity} units out of stock, only if that many are left.
     * The check and the write are one statement, so concurrent orders can't both read the same
     * stock and oversell it. Returns 1 when the units were reserved, 0 when stock was too low.
     */
    @Modifying(flushAutomatically = true)
    @Query("update Product p set p.stockQty = p.stockQty - :quantity, p.updatedAt = CURRENT_TIMESTAMP "
            + "where p.id = :id and p.stockQty >= :quantity")
    int decrementStockIfAvailable(@Param("id") Long id, @Param("quantity") int quantity);
}
