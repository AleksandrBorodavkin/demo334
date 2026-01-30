package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API контроллер
 * Демонстрирует:
 * 1. REST эндпоинты с Spring Security
 * 2. Возврат JSON вместо HTML
 * 3. Использование @PreAuthorize для API
 */
@RestController
@RequestMapping("/api")
public class ApiController {
    
    /**
     * Публичный API эндпоинт
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicApi() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Это публичный API эндпоинт");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Защищенный API эндпоинт для пользователей
     */
    @GetMapping("/user/info")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> userInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities().stream()
            .map(a -> a.getAuthority())
            .toList());
        response.put("authenticated", auth.isAuthenticated());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Защищенный API эндпоинт для администраторов
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adminStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", 100);
        response.put("activeUsers", 75);
        response.put("message", "Статистика доступна только администраторам");
        
        return ResponseEntity.ok(response);
    }
}
