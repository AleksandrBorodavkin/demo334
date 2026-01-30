package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Основная конфигурация Spring Security
 * 
 * Этот класс демонстрирует:
 * 1. Настройку SecurityFilterChain
 * 2. Различные методы аутентификации (in-memory, JPA, JWT)
 * 3. Настройку авторизации (роли, URL patterns)
 * 4. Настройку CSRF защиты
 * 5. Настройку сессий
 * 6. Password encoding
 * 7. Custom authentication provider
 * 8. JWT фильтр для stateless аутентификации
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Включает @PreAuthorize, @PostAuthorize, @Secured
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * SecurityFilterChain - основной фильтр цепочки безопасности
     * 
     * Демонстрирует:
     * - Настройку доступа к URL
     * - Настройку формы логина
     * - Настройку logout
     * - Настройку исключений
     * - Настройку CSRF
     * - Настройку сессий
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF для демонстрации (в продакшене должно быть включено)
                // CSRF защита предотвращает атаки Cross-Site Request Forgery
                .csrf(AbstractHttpConfigurer::disable)

                // Настройка авторизации запросов
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты (доступны всем)
                        .requestMatchers("/", "/public/**", "/login", "/register", "/h2-console/**").permitAll()

                        // JWT аутентификация эндпоинты (публичные)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Эндпоинты только для аутентифицированных пользователей
                        .requestMatchers("/user/**").authenticated()

                        // Эндпоинты только для пользователей с ролью USER
                        .requestMatchers("/user/profile").hasRole("USER")

                        // Эндпоинты только для пользователей с ролью ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Эндпоинты для пользователей с любой из ролей
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated())

                // Добавляем JWT фильтр перед UsernamePasswordAuthenticationFilter
                // JWT фильтр будет проверять токен в заголовке Authorization
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Настройка формы логина
                .formLogin(form -> form
                        .loginPage("/login") // Кастомная страница логина
                        .defaultSuccessUrl("/user/dashboard", true) // Редирект после успешного логина
                        .failureUrl("/login?error=true") // Редирект при ошибке
                        .permitAll())

                // Настройка logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                // Настройка сессий
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Создавать сессию при необходимости
                        .maximumSessions(1) // Максимум 1 сессия на пользователя
                        .maxSessionsPreventsLogin(false) // Разрешить новую сессию, закрыв старую
                )

                // Настройка исключений
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied") // Страница при отказе в доступе
                );

        // Для H2 Console (только для разработки)
        http.headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }

    /**
     * DaoAuthenticationProvider - провайдер аутентификации через UserDetailsService
     * 
     * Демонстрирует:
     * - Использование UserDetailsService для загрузки пользователей
     * - Password encoding при проверке пароля
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager - менеджер аутентификации
     * 
     * Используется для программной аутентификации пользователей
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * PasswordEncoder - кодировщик паролей
     * 
     * BCryptPasswordEncoder использует алгоритм BCrypt для хеширования паролей
     * Это обеспечивает безопасное хранение паролей в базе данных
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strength 12 (рекомендуется)
    }

    /**
     * In-Memory UserDetailsService для демонстрации
     * 
     * Альтернативный способ аутентификации - хранение пользователей в памяти
     * Полезно для тестирования и простых приложений
     */
    @Bean
    public UserDetailsService inMemoryUserDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "USER")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }
}
