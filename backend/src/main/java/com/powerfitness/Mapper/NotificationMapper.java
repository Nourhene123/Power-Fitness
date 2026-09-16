package com.powerfitness.Mapper;

import com.powerfitness.DTO.NotificationDto;
import com.powerfitness.Entity.Notification;
import org.mapstruct.Mapper;

@Mapper
public interface NotificationMapper {

    NotificationDto toDto(Notification notification);
}
