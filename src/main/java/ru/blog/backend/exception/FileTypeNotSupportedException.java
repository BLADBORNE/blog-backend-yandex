package ru.blog.backend.exception;

public class FileTypeNotSupportedException extends RuntimeException {

    public FileTypeNotSupportedException(String message) {
        super(message);
    }
}
