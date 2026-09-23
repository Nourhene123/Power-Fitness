package com.powerfitness.mapper;

import com.powerfitness.dto.NotificationDto;
import com.powerfitness.entity.Notification;
import org.mapstruct.Mapper;

@Mapper
public interface NotificationMapper {

    NotificationDto toDto(Notification notification);
}
