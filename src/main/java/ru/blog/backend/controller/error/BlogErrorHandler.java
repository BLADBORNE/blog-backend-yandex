package ru.blog.backend.controller.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.blog.backend.exception.FileTypeNotSupportedException;
import ru.blog.backend.model.error.ApiError;
import ru.blog.backend.model.error.ErrorMessage;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice("ru.blog.backend.controller")
@Slf4j
public class BlogErrorHandler {

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        ConstraintViolationException.class,
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final Exception e) {

        String message = e.getMessage();

        if (e instanceof MethodArgumentNotValidException exception) {

            message = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        }

        log.error("{}: {}", ErrorMessage.BAD_REQUEST, e.getMessage());

        return createApiError(
            HttpStatus.BAD_REQUEST,
            ErrorMessage.BAD_REQUEST,
            e.getClass().getSimpleName(),
            message
        );
    }

    @ExceptionHandler({
        FileTypeNotSupportedException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final RuntimeException e) {

        log.error("{}: {}", ErrorMessage.CONFLICT, e.getMessage());

        return createApiError(
            HttpStatus.CONFLICT,
            ErrorMessage.CONFLICT,
            e.getClass().getSimpleName(),
            e.getMessage()
        );
    }

    @ExceptionHandler({
        NoSuchElementException.class,
        FileNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final Exception e) {

        log.error("{}: {}", ErrorMessage.NOT_FOUND, e.getMessage());

        return createApiError(
            HttpStatus.NOT_FOUND,
            ErrorMessage.NOT_FOUND,
            e.getClass().getSimpleName(),
            e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowableException(final Throwable e) {

        log.error("{}: {}", ErrorMessage.INTERNAL_SERVER_ERROR, e.getMessage());

        return createApiError(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorMessage.INTERNAL_SERVER_ERROR,
            e.getClass().getSimpleName(),
            e.getMessage()
        );
    }

    private ApiError createApiError(HttpStatus status, ErrorMessage reason, String errorClass, String errorMessage) {

        return ApiError
            .builder()
            .status(status)
            .reason(reason.getDescription())
            .errorClass(errorClass)
            .message(errorMessage)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
