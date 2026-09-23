package com.powerfitness.repository;

import com.powerfitness.entity.ShopOrder;
import com.powerfitness.entity.ShopOrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopOrderItemRepository extends JpaRepository<ShopOrderItem, Long> {

    List<ShopOrderItem> findByOrderOrderByIdAsc(ShopOrder order);
}
