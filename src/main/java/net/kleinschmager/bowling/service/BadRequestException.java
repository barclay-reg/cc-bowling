package net.kleinschmager.bowling.service;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
