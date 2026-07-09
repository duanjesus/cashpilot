package com.cashpilot.service;

import com.cashpilot.dto.request.UpdateUserRequestDTO;
import com.cashpilot.dto.response.UserResponseDTO;

public interface UserService {

    UserResponseDTO getCurrentUser();

    UserResponseDTO updateCurrentUser(UpdateUserRequestDTO dto);

}
