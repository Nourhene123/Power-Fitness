package com.powerfitness.repository;

import com.powerfitness.entity.OrderStatus;
import com.powerfitness.entity.ShopOrder;
import com.powerfitness.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    List<ShopOrder> findByUserOrderByCreatedAtDesc(User user);

    List<ShopOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<ShopOrder> findAllByOrderByCreatedAtDesc();
}
