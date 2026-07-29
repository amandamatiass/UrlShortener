package org.acme.shortener.exception;

public class CodeAlreadyInUseException extends RuntimeException {
    public CodeAlreadyInUseException(String code) {
        super("O código '" + code + "' já está em uso");
    }
}
