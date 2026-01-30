package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для пользователей
 * Демонстрирует:
 * 1. Доступ только для аутентифицированных пользователей
 * 2. Использование @PreAuthorize для проверки ролей
 * 3. Получение информации о текущем пользователе
 */
@Controller
@RequestMapping("/user")
public class UserController {
    
    /**
     * Dashboard пользователя
     * Доступен только аутентифицированным пользователям
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Получение информации о текущем аутентифицированном пользователе
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        model.addAttribute("username", auth.getName());
        model.addAttribute("authorities", auth.getAuthorities());
        model.addAttribute("isAuthenticated", auth.isAuthenticated());
        
        return "user-dashboard";
    }
    
    /**
     * Профиль пользователя
     * Доступен только пользователям с ролью USER (настроено в SecurityConfig)
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        return "user-profile";
    }
    
    /**
     * Метод с аннотацией @PreAuthorize
     * Демонстрирует метод-уровневую безопасность
     */
    @GetMapping("/settings")
    @PreAuthorize("hasRole('USER')")
    public String settings(Model model) {
        model.addAttribute("message", "Настройки пользователя");
        return "user-settings";
    }
}
