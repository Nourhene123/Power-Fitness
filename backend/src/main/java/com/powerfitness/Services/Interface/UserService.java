package com.powerfitness.Services.Interface;

import com.powerfitness.DTO.UserDto;

public interface UserService {

    UserDto currentUser(Long userId);

    UserDto updateProfile(Long userId, String name);
}
