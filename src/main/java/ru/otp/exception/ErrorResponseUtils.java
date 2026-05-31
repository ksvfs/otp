package ru.otp.exception;

import org.springframework.http.HttpStatus;

public final class ErrorResponseUtils {

    private ErrorResponseUtils() {
    }

    public static String toRussianStatus(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Некорректный запрос";
            case UNAUTHORIZED -> "Требуется аутентификация";
            case FORBIDDEN -> "Доступ запрещен";
            case NOT_FOUND -> "Ресурс не найден";
            case METHOD_NOT_ALLOWED -> "Метод не поддерживается";
            case CONFLICT -> "Конфликт данных";
            case UNSUPPORTED_MEDIA_TYPE -> "Неподдерживаемый тип содержимого";
            case INTERNAL_SERVER_ERROR -> "Внутренняя ошибка сервера";
            default -> status.getReasonPhrase();
        };
    }
}
