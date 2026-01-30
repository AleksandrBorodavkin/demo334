package com.example.demo.controller;

import com.example.demo.dto.JwtResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для JWT аутентификации
 * 
 * Демонстрирует:
 * 1. Аутентификацию пользователя по username/password
 * 2. Генерацию JWT токена после успешной аутентификации
 * 3. Возврат токена клиенту для использования в последующих запросах
 * 
 * JWT токен используется для stateless аутентификации в REST API.
 * Клиент отправляет токен в заголовке Authorization: Bearer <token>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthJwtController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Эндпоинт для аутентификации и получения JWT токена
     * 
     * @param loginRequest запрос с username и password
     * @return JWT токен и информация о пользователе
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Создаем объект аутентификации
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            // Устанавливаем аутентификацию в SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Генерируем JWT токен
            String jwt = tokenProvider.generateToken(authentication);

            // Получаем информацию о пользователе
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String authorities = authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            // Возвращаем токен и информацию о пользователе
            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getUsername(),
                    authorities));

        } catch (Exception e) {
            // В случае ошибки аутентификации
            return ResponseEntity.badRequest()
                    .body("Неверное имя пользователя или пароль");
        }
    }

    /**
     * Эндпоинт для проверки валидности токена
     * 
     * @return информация о текущем пользователе из токена
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok().body("Токен валиден. Пользователь: " + authentication.getName());
        }

        return ResponseEntity.badRequest().body("Токен невалиден или отсутствует");
    }
}
