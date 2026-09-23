package com.powerfitness.service.impl;

import com.powerfitness.dto.NotificationDto;
import com.powerfitness.entity.Notification;
import com.powerfitness.entity.Role;
import com.powerfitness.entity.User;
import com.powerfitness.exception.ResourceNotFoundException;
import com.powerfitness.mapper.NotificationMapper;
import com.powerfitness.repository.NotificationRepository;
import com.powerfitness.repository.UserRepository;
import com.powerfitness.service.NotificationService;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notifications;
    private final UserRepository users;
    private final NotificationMapper mapper;

    public NotificationServiceImpl(NotificationRepository notifications, UserRepository users,
                                   NotificationMapper mapper) {
        this.notifications = notifications;
        this.users = users;
        this.mapper = mapper;
    }

    @Override
    public void notify(User user, String type, String title, String body, String link) {
        notifications.save(Notification.builder()
                .user(user).type(type).title(title).body(body).link(link)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> recent(Long userId, int limit) {
        User user = user(userId);
        return notifications.findByUserOrderByCreatedAtDesc(user, Limit.of(limit)).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notifications.countByUserAndReadAtIsNull(user(userId));
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        Notification n = notifications.findById(notificationId)
                .filter(x -> x.getUser().getId().equals(userId))
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", notificationId));
        if (n.getReadAt() == null) {
            n.setReadAt(Instant.now());
        }
    }

    @Override
    public void markAllRead(Long userId) {
        notifications.markAllRead(user(userId), Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> coachUser() {
        return users.findByRoleIn(List.of(Role.COACH, Role.ADMIN)).stream()
                .min(Comparator.comparing((User u) -> u.getRole() == Role.COACH ? 0 : 1)
                        .thenComparing(User::getId));
    }

    private User user(Long id) {
        return users.findById(id).orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
