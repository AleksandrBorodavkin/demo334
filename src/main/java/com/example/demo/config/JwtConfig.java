package com.example.demo.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Конфигурация JWT используя официальные библиотеки Spring Security
 * 
 * Демонстрирует создание JwtEncoder и JwtDecoder через Spring Security OAuth2
 * JOSE
 * 
 * Использует:
 * - NimbusJwtEncoder с JWKSource для создания токенов
 * - NimbusJwtDecoder с секретным ключом для декодирования токенов
 * - MacAlgorithm.HS512 для подписи токенов (HMAC-SHA512)
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Создание секретного ключа для подписи JWT токенов
     * Используется HMAC-SHA512 алгоритм
     * Для HS512 нужен ключ минимум 512 бит (64 байта)
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // Для HS512 нужен ключ минимум 512 бит (64 байта)
        if (keyBytes.length < 64) {
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 64));
            keyBytes = paddedKey;
        } else if (keyBytes.length > 64) {
            // Обрезаем до 64 байт если длиннее
            byte[] trimmedKey = new byte[64];
            System.arraycopy(keyBytes, 0, trimmedKey, 0, 64);
            keyBytes = trimmedKey;
        }
        return new SecretKeySpec(keyBytes, "HmacSHA512");
    }

    /**
     * JwtEncoder для создания JWT токенов
     * Использует NimbusJwtEncoder с JWKSource
     * 
     * Для симметричного шифрования (shared secret) используется OctetSequenceKey
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKey secretKey = getSecretKey();

        // Создаем OctetSequenceKey из секретного ключа
        OctetSequenceKey octetKey = new OctetSequenceKey.Builder(secretKey)
                .keyID("jwt-signing-key")
                .build();

        // Создаем JWKSet с нашим ключом
        JWKSet jwkSet = new JWKSet(octetKey);

        // Создаем JWKSource для NimbusJwtEncoder
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * JwtDecoder для декодирования и валидации JWT токенов
     * Использует NimbusJwtDecoder с секретным ключом и алгоритмом HS512
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = getSecretKey();
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }
}
