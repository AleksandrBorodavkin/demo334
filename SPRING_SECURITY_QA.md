# Spring Security — вопросы и ответы

Файл в формате вопросов и ответов по Spring Security на основе демо-проекта.

---

## Основы Spring Security

### Что такое Spring Security?

**Ответ:** Spring Security — это фреймворк для обеспечения безопасности в Spring приложениях. Он предоставляет:

- **Аутентификацию** — проверка личности пользователя (логин/пароль)
- **Авторизацию** — проверка прав доступа к ресурсам
- **Защиту от атак** — CSRF, XSS и другие

---

### В чём разница между Authentication и Authorization?

**Ответ:**

- **Authentication (аутентификация)** — ответ на вопрос «Кто вы?»: проверка личности пользователя (логин и пароль).
- **Authorization (авторизация)** — ответ на вопрос «Что вы можете делать?»: проверка прав доступа к ресурсам.

Сначала выполняется аутентификация, затем авторизация.

---

### Что такое SecurityFilterChain?

**Ответ:** SecurityFilterChain — это цепочка фильтров, которая обрабатывает каждый HTTP-запрос до того, как он дойдёт до контроллера. Фильтры отвечают за:

- проверку аутентификации пользователя;
- проверку авторизации (прав доступа);
- проверку CSRF-токенов;
- другие аспекты безопасности.

---

## UserDetailsService и аутентификация

### Что такое UserDetailsService?

**Ответ:** UserDetailsService — интерфейс Spring Security для загрузки данных пользователя. В нём один метод:

```java
UserDetails loadUserByUsername(String username)
```

Spring Security вызывает его при аутентификации, чтобы получить объект `UserDetails` (логин, пароль, роли) по имени пользователя.

---

### Как работает CustomUserDetailsService?

**Ответ:** Последовательность шагов:

1. Пользователь вводит логин и пароль.
2. Spring Security вызывает `loadUserByUsername(username)`.
3. Сервис загружает пользователя из БД.
4. Роли преобразуются в `GrantedAuthority`.
5. Возвращается объект `UserDetails`.
6. Spring Security проверяет пароль через `PasswordEncoder`.
7. При совпадении пароля пользователь считается аутентифицированным.

---

### Зачем нужен PasswordEncoder?

**Ответ:** PasswordEncoder нужен для безопасного хранения паролей:

- Пароли **не хранятся в открытом виде**.
- Даже администратор БД не видит реальные пароли.
- Используется **одностороннее хеширование** (восстановить пароль по хешу нельзя).

В проекте используется BCrypt (алгоритм `BCryptPasswordEncoder`).

---

## Авторизация

### Как работает авторизация на основе ролей?

**Ответ:**

1. У пользователя есть одна или несколько ролей (USER, ADMIN и т.д.).
2. Роли хранятся в БД и связаны с пользователем связью Many-to-Many.
3. При аутентификации роли преобразуются в `GrantedAuthority`.
4. Spring Security проверяет роли при обращении к защищённым URL или методам.

---

### Что такое @PreAuthorize?

**Ответ:** @PreAuthorize — аннотация для **метод-уровневой** авторизации. Права проверяются **до** выполнения метода.

Пример:

```java
@PreAuthorize("hasRole('ADMIN')")
public String adminMethod() { ... }
```

Если у пользователя нет роли ADMIN, метод не выполнится и будет выброшено `AccessDeniedException`.

---

### В чём разница между hasRole() и hasAuthority()?

**Ответ:**

- **hasRole("ADMIN")** — Spring Security сам добавляет префикс `"ROLE_"` и проверяет наличие `"ROLE_ADMIN"`.
- **hasAuthority("ROLE_ADMIN")** — проверяется точное совпадение строки `"ROLE_ADMIN"`.

В проекте роли хранятся с префиксом `"ROLE_"`, поэтому удобнее использовать `hasRole()`.

---

### Как добавить новую роль?

**Ответ:**

1. Создать роль в БД через `RoleRepository`.
2. Назначить роль пользователю через `UserRepository`.
3. Обновить правила в `SecurityConfig` (например, `requestMatchers`) или использовать `@PreAuthorize` на методах.

---

### Что такое GrantedAuthority?

**Ответ:** GrantedAuthority — интерфейс, описывающий право доступа. В Spring Security роли обычно представляются как `GrantedAuthority` с префиксом `"ROLE_"`.

Пример: роль `"ADMIN"` в коде часто хранится как `"ROLE_ADMIN"` в виде `GrantedAuthority`.

---

## Сессии и CSRF

### Как управляются сессии?

**Ответ:** В `SecurityConfig` задаётся, например:

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    .maximumSessions(1)
    .maxSessionsPreventsLogin(false)
)
```

- **IF_REQUIRED** — сессия создаётся при необходимости.
- **maximumSessions(1)** — не более одной активной сессии на пользователя.
- **maxSessionsPreventsLogin(false)** — новая сессия может «вытеснить» старую.

---

### Как работает защита от CSRF?

**Ответ:** **CSRF (Cross-Site Request Forgery)** — атака, при которой пользователь без своего ведома отправляет запрос от своего имени.

Spring Security помогает защититься так:

- Генерирует уникальный CSRF-токен для сессии.
- Ожидает этот токен в запросах POST/PUT/DELETE.
- Проверяет токен перед обработкой запроса.

В демо-проекте CSRF отключён для упрощения; в продакшене его нужно включать.

---

## REST API и JWT

### Как защитить REST API?

**Ответ:** В проекте показаны такие приёмы:

- Использование `@RestController` и возврат JSON.
- Аннотации `@PreAuthorize` на методах API.
- **JWT-токены** для stateless-аутентификации.

Пример:

```java
@GetMapping("/api/user/info")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<Map<String, Object>> userInfo() { ... }
```

---

### Какую роль выполняет JWT в проекте?

**Ответ:** JWT используется для **stateless-аутентификации REST API**:

1. Клиент отправляет POST на `/api/auth/login` с логином и паролем.
2. Сервер проверяет учётные данные и возвращает JWT-токен.
3. Клиент при следующих запросах передаёт токен в заголовке: `Authorization: Bearer <token>`.
4. `JwtAuthenticationFilter` извлекает токен, проверяет его и заполняет `SecurityContext`.

Для веб-страниц по-прежнему используется аутентификация через форму и сессии; для API — JWT.

---

### Как работает JWT-аутентификация?

**Ответ:**

**Получение токена:**

1. POST на `/api/auth/login` с username и password.
2. Сервер проверяет данные через `AuthenticationManager`.
3. При успехе создаётся JWT через `JwtTokenProvider` (JwtEncoder).
4. Токен возвращается клиенту.

**Использование токена:**

1. Клиент добавляет заголовок `Authorization: Bearer <token>`.
2. `JwtAuthenticationFilter` извлекает токен.
3. Токен проверяется через `JwtDecoder`.
4. Из токена извлекаются username и роли.
5. Создаётся объект `Authentication` и помещается в `SecurityContext`.

**Валидация:** проверяются подпись токена и срок действия (exp).

---

### В чём разница между JWT и Session-based аутентификацией?

**Ответ:**

| Аспект            | JWT                    | Session-based                          |
|-------------------|------------------------|----------------------------------------|
| Хранение          | Токен у клиента        | Session ID у клиента, данные на сервере |
| Stateless         | Да                     | Нет                                    |
| Масштабирование   | Проще (нет сессий)     | Нужно общее хранилище (Redis, БД)      |
| Отзыв доступа     | Сложнее (blacklist)    | Проще (удалить сессию)                 |
| Размер            | Больше (есть payload)  | Меньше (только ID)                     |
| Типичное использование | REST API, микросервисы | Веб-приложения с формами                |

В проекте: веб — session-based, API — JWT.

---

### Как работает JwtAuthenticationFilter?

**Ответ:** Фильтр для каждого запроса:

1. Извлекает токен из заголовка `Authorization: Bearer <token>`.
2. Проверяет токен через `JwtTokenProvider` (валидность, срок действия).
3. Достаёт из токена username и роли.
4. Создаёт объект `Authentication`.
5. Устанавливает его в `SecurityContext`.

Он стоит в цепочке **перед** `UsernamePasswordAuthenticationFilter`: если есть валидный JWT — используется он; если нет — срабатывает форма логина.

---

### Как настроить время жизни JWT-токена?

**Ответ:** В `application.properties`:

```properties
jwt.expiration=86400000  # 24 часа в миллисекундах
```

В коде:

```java
@Value("${jwt.expiration}")
private long jwtExpirationMs;
```

Рекомендации: access token — от 15 минут до 1 часа; refresh token (если есть) — 7–30 дней.

---

## Модель данных

### Как устроена связь User–Role (Many-to-Many)?

**Ответ:**

- Таблица **users** — пользователи.
- Таблица **roles** — роли.
- Таблица **user_roles** — связь «многие ко многим».

Один пользователь может иметь несколько ролей, одна роль — у многих пользователей.

---

## Дополнительно

### Какие библиотеки Spring используются для JWT?

**Ответ:** В проекте используются официальные модули Spring Security:

- **spring-security-oauth2-jose** — работа с JWT (JOSE).
- **spring-security-oauth2-resource-server** — OAuth2 Resource Server.
- **NimbusJwtEncoder** и **NimbusJwtDecoder** — реализация на базе Nimbus JOSE + JWT SDK.

---

### Как получить и использовать JWT в проекте?

**Ответ:**

```bash
# 1. Получить токен
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Использовать токен в запросах
curl http://localhost:8080/api/user/info \
  -H "Authorization: Bearer <полученный_токен>"
```

---

*Документ составлен по демо-проекту Spring Security. Подробности — в [README.md](README.md).*
