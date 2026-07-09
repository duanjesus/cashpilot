package com.cashpilot.dto.response;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        Boolean active
) {
}
