package com.cashpilot.security;

import com.cashpilot.entity.User;
import com.cashpilot.exception.ResourceNotFoundException;
import com.cashpilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the {@link User} entity for the currently authenticated principal.
 * Every service that owns per-user data calls this to scope its queries.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

}
