package com.powerfitness.Repository;

import com.powerfitness.Entity.OrderStatus;
import com.powerfitness.Entity.ShopOrder;
import com.powerfitness.Entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    List<ShopOrder> findByUserOrderByCreatedAtDesc(User user);

    List<ShopOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<ShopOrder> findAllByOrderByCreatedAtDesc();
}
