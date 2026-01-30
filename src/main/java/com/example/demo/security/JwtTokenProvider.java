package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * JWT Token Provider используя официальные библиотеки Spring Security
 * 
 * Этот класс демонстрирует:
 * 1. Генерацию JWT токенов через Spring Security JwtEncoder
 * 2. Валидацию JWT токенов через Spring Security JwtDecoder
 * 3. Извлечение информации из токена (username, роли)
 * 4. Проверку срока действия токена
 * 
 * Использует Spring Security OAuth2 JOSE библиотеки:
 * - JwtEncoder для создания токенов (инжектируется как Bean)
 * - JwtDecoder для декодирования токенов (инжектируется как Bean)
 * - MacAlgorithm.HS512 для подписи токенов
 * 
 * JWT (JSON Web Token) - это стандарт для создания токенов доступа,
 * который позволяет передавать информацию между сторонами в безопасном виде.
 * 
 * Структура JWT:
 * - Header: алгоритм и тип токена
 * - Payload: данные (claims) - username, роли, время истечения
 * - Signature: подпись для проверки подлинности
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Autowired
    public JwtTokenProvider(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Генерация JWT токена для пользователя
     * 
     * @param authentication объект аутентификации Spring Security
     * @return JWT токен в виде строки
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Получаем роли пользователя
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(jwtExpirationMs);

        // Создаем claims для JWT токена
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spring-security-demo")
                .issuedAt(now)
                .expiresAt(expiryDate)
                .subject(userDetails.getUsername())
                .claim("authorities", authorities)
                .build();

        // Создаем header
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();

        // Кодируем токен
        JwtEncoderParameters encoderParameters = JwtEncoderParameters.from(header, claims);
        Jwt jwt = jwtEncoder.encode(encoderParameters);

        return jwt.getTokenValue();
    }

    /**
     * Генерация JWT токена из UserDetails
     * 
     * @param userDetails детали пользователя
     * @return JWT токен в виде строки
     */
    public String generateToken(UserDetails userDetails) {
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(jwtExpirationMs);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spring-security-demo")
                .issuedAt(now)
                .expiresAt(expiryDate)
                .subject(userDetails.getUsername())
                .claim("authorities", authorities)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();

        JwtEncoderParameters encoderParameters = JwtEncoderParameters.from(header, claims);
        Jwt jwt = jwtEncoder.encode(encoderParameters);

        return jwt.getTokenValue();
    }

    /**
     * Декодирование JWT токена
     * 
     * @param token JWT токен
     * @return объект Jwt с claims
     */
    public Jwt decodeToken(String token) {
        return jwtDecoder.decode(token);
    }

    /**
     * Извлечение имени пользователя из токена
     * 
     * @param token JWT токен
     * @return имя пользователя
     */
    public String getUsernameFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getSubject();
    }

    /**
     * Извлечение ролей из токена
     * 
     * @param token JWT токен
     * @return строку с ролями, разделенными пробелами
     */
    public String getAuthoritiesFromToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return jwt.getClaim("authorities");
    }

    /**
     * Валидация JWT токена
     * 
     * @param token JWT токен для проверки
     * @return true если токен валиден, false в противном случае
     */
    public boolean validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            // Проверяем, что токен не истек
            return jwt.getExpiresAt() != null && jwt.getExpiresAt().isAfter(Instant.now());
        } catch (Exception e) {
            // Токен невалиден (истек, неправильная подпись и т.д.)
            return false;
        }
    }

    /**
     * Проверка истечения токена
     * 
     * @param token JWT токен
     * @return true если токен истек, false если еще действителен
     */
    public boolean isTokenExpired(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            if (jwt.getExpiresAt() == null) {
                return true;
            }
            return jwt.getExpiresAt().isBefore(Instant.now());
        } catch (Exception e) {
            return true;
        }
    }
}
