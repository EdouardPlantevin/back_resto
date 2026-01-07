package com.edouard.back_resto.exception;

public class RefreshTokenInvalidException extends RuntimeException {
    public RefreshTokenInvalidException() {
        super("Refresh token is expired or revoked");
    }
}
