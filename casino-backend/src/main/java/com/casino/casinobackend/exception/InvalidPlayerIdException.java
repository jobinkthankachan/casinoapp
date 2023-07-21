package com.casino.casinobackend.exception;

public class InvalidPlayerIdException extends RuntimeException {
    public InvalidPlayerIdException(String message) {
        super(message);
    }
}
