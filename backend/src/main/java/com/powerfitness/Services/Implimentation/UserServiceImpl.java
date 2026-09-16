package com.powerfitness.Services.Implimentation;

import com.powerfitness.DTO.UserDto;
import com.powerfitness.Entity.User;
import com.powerfitness.Exception.ResourceNotFoundException;
import com.powerfitness.Mapper.UserMapper;
import com.powerfitness.Repository.AssessmentRepository;
import com.powerfitness.Repository.UserRepository;
import com.powerfitness.Services.Interface.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository users;
    private final AssessmentRepository assessments;
    private final UserMapper mapper;

    public UserServiceImpl(UserRepository users, AssessmentRepository assessments, UserMapper mapper) {
        this.users = users;
        this.assessments = assessments;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto currentUser(Long userId) {
        User user = load(userId);
        return mapper.toDto(user, assessments.existsByUser(user));
    }

    @Override
    public UserDto updateProfile(Long userId, String name) {
        User user = load(userId);
        user.setName(name.trim());
        return mapper.toDto(user, assessments.existsByUser(user));
    }

    private User load(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }
}
