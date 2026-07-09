package com.cashpilot.service.impl;

import com.cashpilot.dto.request.UpdateUserRequestDTO;
import com.cashpilot.dto.response.UserResponseDTO;
import com.cashpilot.entity.User;
import com.cashpilot.security.CurrentUserProvider;
import com.cashpilot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser() {
        return toResponseDto(currentUserProvider.getCurrentUser());
    }

    @Override
    public UserResponseDTO updateCurrentUser(UpdateUserRequestDTO dto) {
        User user = currentUserProvider.getCurrentUser();
        user.setName(dto.name());
        return toResponseDto(user);
    }

    private UserResponseDTO toResponseDto(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getActive());
    }

}
