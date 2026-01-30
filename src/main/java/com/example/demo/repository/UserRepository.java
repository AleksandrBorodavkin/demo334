package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с пользователями
 * Используется UserDetailsService для загрузки пользователей из БД
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Поиск пользователя по имени
     * Используется Spring Security для аутентификации
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Проверка существования пользователя
     */
    boolean existsByUsername(String username);
}
