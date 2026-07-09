package com.cashpilot.service.impl;

import com.cashpilot.dto.request.LoginRequestDTO;
import com.cashpilot.dto.request.RegisterRequestDTO;
import com.cashpilot.dto.response.AuthResponseDTO;
import com.cashpilot.entity.User;
import com.cashpilot.exception.DuplicateResourceException;
import com.cashpilot.exception.InvalidCredentialsException;
import com.cashpilot.repository.UserRepository;
import com.cashpilot.security.JwtService;
import com.cashpilot.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("There is already a user registered with the email: " + dto.email());
        }

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .active(true)
                .build();

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        return buildAuthResponse(user);
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        return AuthResponseDTO.of(token, jwtService.getExpirationMs(), user.getName(), user.getEmail());
    }

}
