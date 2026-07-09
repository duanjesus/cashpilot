package com.cashpilot.service;

import com.cashpilot.dto.request.LoginRequestDTO;
import com.cashpilot.dto.request.RegisterRequestDTO;
import com.cashpilot.dto.response.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO register(RegisterRequestDTO dto);

    AuthResponseDTO login(LoginRequestDTO dto);

}
