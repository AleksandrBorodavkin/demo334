package com.example.demo.controller;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.Set;

/**
 * Контроллер для регистрации и аутентификации
 * Демонстрирует:
 * 1. Регистрацию новых пользователей
 * 2. Кодирование паролей
 * 3. Назначение ролей
 */
@Controller
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Страница регистрации
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        return "register";
    }
    
    /**
     * Обработка регистрации
     */
    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {
        
        // Проверка существования пользователя
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Пользователь с таким именем уже существует");
            return "register";
        }
        
        // Создание нового пользователя
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // Кодирование пароля
        user.setEnabled(true);
        
        // Назначение роли USER по умолчанию
        Role userRole = roleRepository.findByName("USER")
            .orElseGet(() -> {
                Role role = new Role("USER");
                return roleRepository.save(role);
            });
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        userRepository.save(user);
        
        model.addAttribute("success", "Регистрация успешна! Теперь вы можете войти.");
        return "login";
    }
}
