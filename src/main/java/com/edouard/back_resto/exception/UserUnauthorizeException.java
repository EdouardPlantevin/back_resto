package com.edouard.back_resto.exception;

public class UserUnauthorizeException extends RuntimeException {
    public UserUnauthorizeException(String message) {
        super("L'utilisateur n'est pas autorisé à effectuer cette action : " + message);
    }
}
