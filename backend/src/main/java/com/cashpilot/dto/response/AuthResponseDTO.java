package com.cashpilot.dto.response;

public record AuthResponseDTO(
        String token,
        String tokenType,
        Long expiresInMs,
        String name,
        String email
) {

    public static AuthResponseDTO of(String token, Long expiresInMs, String name, String email) {
        return new AuthResponseDTO(token, "Bearer", expiresInMs, name, email);
    }

}
