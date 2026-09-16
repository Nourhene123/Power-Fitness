package com.powerfitness.RestController;

import com.powerfitness.DTO.UpdateProfileRequest;
import com.powerfitness.DTO.UserDto;
import com.powerfitness.Security.AppUserPrincipal;
import com.powerfitness.Services.Interface.UserService;
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
