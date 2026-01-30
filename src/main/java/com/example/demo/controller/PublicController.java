package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Публичный контроллер - демонстрирует эндпоинты без защиты
 * Доступен всем пользователям без аутентификации
 */
@Controller
@RequestMapping("/public")
public class PublicController {
    
    /**
     * Публичная страница - доступна всем
     */
    @GetMapping("/info")
    public String publicInfo(Model model) {
        model.addAttribute("message", "Это публичная информация, доступная всем");
        return "public-info";
    }
}
