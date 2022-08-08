package com.coubdownloader.classes;

public class CoubException extends RuntimeException {
    public CoubException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
