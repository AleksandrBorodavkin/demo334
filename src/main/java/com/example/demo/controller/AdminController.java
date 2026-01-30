package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Контроллер для администраторов
 * Демонстрирует:
 * 1. Доступ только для пользователей с ролью ADMIN
 * 2. Использование @PreAuthorize для проверки ролей на уровне методов
 */
@Controller
@RequestMapping("/admin")
public class AdminController {
    
    /**
     * Админ панель
     * Доступна только пользователям с ролью ADMIN (настроено в SecurityConfig)
     */
    @GetMapping("/panel")
    public String adminPanel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        model.addAttribute("message", "Добро пожаловать в админ панель!");
        return "admin-panel";
    }
    
    /**
     * Управление пользователями
     * Демонстрирует метод-уровневую безопасность с @PreAuthorize
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageUsers(Model model) {
        model.addAttribute("message", "Управление пользователями");
        return "admin-users";
    }
    
    /**
     * Настройки системы
     * Демонстрирует проверку нескольких ролей
     */
    @GetMapping("/settings")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String systemSettings(Model model) {
        model.addAttribute("message", "Настройки системы");
        return "admin-settings";
    }
}
