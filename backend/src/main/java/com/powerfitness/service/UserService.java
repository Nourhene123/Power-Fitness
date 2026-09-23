package com.powerfitness.service;

import com.powerfitness.dto.UserDto;

public interface UserService {

    UserDto currentUser(Long userId);

    UserDto updateProfile(Long userId, String name);
}
