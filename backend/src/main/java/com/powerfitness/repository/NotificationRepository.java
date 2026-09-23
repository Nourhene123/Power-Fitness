package com.powerfitness.repository;

import com.powerfitness.entity.Notification;
import com.powerfitness.entity.User;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user, Limit limit);

    long countByUserAndReadAtIsNull(User user);

    @Modifying
    @Query("update Notification n set n.readAt = :now where n.user = :user and n.readAt is null")
    int markAllRead(@Param("user") User user, @Param("now") Instant now);
}
