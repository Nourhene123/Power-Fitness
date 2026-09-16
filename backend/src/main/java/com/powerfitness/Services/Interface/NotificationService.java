package com.powerfitness.Services.Interface;

import com.powerfitness.DTO.NotificationDto;
import com.powerfitness.Entity.User;
import java.util.List;
import java.util.Optional;

public interface NotificationService {

    void notify(User user, String type, String title, String body, String link);

    List<NotificationDto> recent(Long userId, int limit);

    long unreadCount(Long userId);

    void markRead(Long userId, Long notificationId);

    void markAllRead(Long userId);

    /** The account roadmap drafts are routed to for review. */
    Optional<User> coachUser();
}
