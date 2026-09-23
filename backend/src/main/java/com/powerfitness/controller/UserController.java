package com.powerfitness.controller;

import com.powerfitness.dto.UpdateProfileRequest;
import com.powerfitness.dto.UserDto;
import com.powerfitness.security.AppUserPrincipal;
import com.powerfitness.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/me")
    public UserDto updateProfile(@AuthenticationPrincipal AppUserPrincipal principal,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.id(), request.name());
    }
}
