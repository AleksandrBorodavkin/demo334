package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter
 * 
 * Этот фильтр демонстрирует:
 * 1. Извлечение JWT токена из HTTP заголовка
 * 2. Валидацию токена
 * 3. Создание объекта Authentication из токена
 * 4. Установку Authentication в SecurityContext
 * 
 * Фильтр выполняется один раз для каждого запроса (OncePerRequestFilter)
 * и проверяет наличие JWT токена в заголовке Authorization.
 * 
 * Формат заголовка: "Authorization: Bearer <token>"
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Извлечение JWT токена из HTTP заголовка Authorization
     * 
     * @param request HTTP запрос
     * @return JWT токен или null если токен не найден
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Убираем "Bearer "
        }

        return null;
    }

    /**
     * Основной метод фильтра
     * Выполняется для каждого HTTP запроса
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Извлекаем JWT токен из запроса
            String jwt = getJwtFromRequest(request);

            // Если токен найден и валиден
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Извлекаем имя пользователя из токена
                String username = tokenProvider.getUsernameFromToken(jwt);

                // Извлекаем роли из токена
                String authoritiesString = tokenProvider.getAuthoritiesFromToken(jwt);

                // Преобразуем роли в список GrantedAuthority
                // В Spring Security JWT роли разделяются пробелами, а не запятыми
                List<SimpleGrantedAuthority> authorities = Arrays.stream(authoritiesString.split(" "))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // Создаем объект Authentication
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities);

                // Устанавливаем детали аутентификации
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Устанавливаем Authentication в SecurityContext
                // Теперь Spring Security знает, что пользователь аутентифицирован
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Логируем ошибку (в реальном приложении)
            logger.error("Could not set user authentication in security context", ex);
        }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }
}
