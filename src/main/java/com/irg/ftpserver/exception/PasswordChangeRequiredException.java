package com.irg.ftpserver.exception;

public class PasswordChangeRequiredException extends RuntimeException{
    public PasswordChangeRequiredException(String message) {
        super(message);
    }
}
