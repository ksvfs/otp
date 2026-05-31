# OTP

Сервис временных кодов подтверждения для защиты операций. Приложение реализовано на Spring Boot, Java, PostgreSQL и JDBC.

## Возможности

- регистрация и логин пользователей
- JWT-аутентификация и разграничение ролей `ADMIN` и `USER`
- генерация OTP-кодов для операций
- валидация OTP-кодов
- настройка длины и времени жизни OTP-кода
- рассылка OTP через email, SMPP SMS, Telegram и сохранение в файл
- фоновая пометка просроченных кодов в статус `EXPIRED`
- логирование всех HTTP-запросов и бизнес-событий

## Архитектура

Проект разделен на слои:

- `controller` HTTP API
- `service` бизнес-логика
- `dao` работа с PostgreSQL через JDBC `JdbcTemplate`
- `notification` каналы доставки кодов
- `security` JWT и правила доступа
- `scheduler` фоновая обработка просроченных кодов

## Таблицы базы данных

Приложение использует три основные таблицы:

- `users` логин, пароль в хешированном виде, роль и контакты пользователя
- `otp_config` актуальная конфигурация OTP-кодов
- `otp_codes` OTP-коды, статус, идентификатор операции и время жизни

Статусы OTP:

- `ACTIVE`
- `EXPIRED`
- `USED`

## Запуск

```bash
docker compose up --build
```

После запуска будут доступны:

- API приложения: `http://localhost:8080`
- MailHog UI: `http://localhost:8025`
- SMPPsim UI: `http://localhost:8088`
- PostgreSQL: `localhost:5432`

Файл с OTP-кодами для канала `FILE` будет сохраняться в `otp-codes.log` в корне проекта.

## Telegram

Для отправки в Telegram перед запуском нужно задать переменные окружения:

```bash
export APP_TELEGRAM_BOT_TOKEN=токен_бота
export APP_TELEGRAM_DEFAULT_CHAT_ID=chat_id
docker compose up --build
```

## Основные API

### Регистрация

`POST /api/auth/register`

```json
{
  "username": "admin",
  "password": "password",
  "role": "ADMIN",
  "email": "admin@example.com",
  "phone": "+79650000000",
  "telegramChatId": "999999999"
}
```

В системе может существовать только один администратор.

### Логин

`POST /api/auth/login`

```json
{
  "username": "admin",
  "password": "password"
}
```

Ответ содержит JWT-токен.

### Получение конфигурации OTP

`GET /api/admin/config`

Нужен токен администратора.

### Изменение конфигурации OTP

`PUT /api/admin/config`

```json
{
  "codeLength": 6,
  "ttlSeconds": 300
}
```

### Список пользователей

`GET /api/admin/users`

Возвращает всех пользователей кроме администраторов.

### Удаление пользователя

`DELETE /api/admin/users/{id}`

Удаляет пользователя и связанные OTP-коды.

### Генерация OTP

`POST /api/user/otp/generate`

```json
{
  "operationId": "operation-1",
  "channels": ["EMAIL", "SMS", "TELEGRAM", "FILE"],
  "email": "user@example.com",
  "phone": "+79660000000",
  "telegramChatId": "123456789"
}
```

Можно передавать один или несколько каналов сразу.

### Валидация OTP

`POST /api/user/otp/validate`

```json
{
  "operationId": "operation-1",
  "code": "223356"
}
```

## Пример сценария через curl

### 1. Зарегистрировать администратора

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"admin",
    "password":"password",
    "role":"ADMIN",
    "email":"admin@example.com"
  }'
```

### 2. Получить токен

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username":"admin",
    "password":"password"
  }'
```

### 3. Зарегистрировать обычного пользователя

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"user1",
    "password":"password",
    "role":"USER",
    "email":"user1@example.com",
    "phone":"+79991112233"
  }'
```

### 4. Сгенерировать OTP-код

```bash
curl -X POST http://localhost:8080/api/user/otp/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ТОКЕН" \
  -d '{
    "operationId":"operation-1",
    "channels":["FILE","EMAIL"]
  }'
```

## Используемые внешние библиотеки

- Spring Boot Web
- Spring Boot JDBC
- Spring Security
- PostgreSQL JDBC Driver
- JJWT
- JSMPP
- Spring Mail

Устанавливаются автоматически при `docker compose up --build`.
