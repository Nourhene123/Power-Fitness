package com.powerfitness.Repository;

import com.powerfitness.Entity.ShopOrder;
import com.powerfitness.Entity.ShopOrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopOrderItemRepository extends JpaRepository<ShopOrderItem, Long> {

    List<ShopOrderItem> findByOrderOrderByIdAsc(ShopOrder order);
}
