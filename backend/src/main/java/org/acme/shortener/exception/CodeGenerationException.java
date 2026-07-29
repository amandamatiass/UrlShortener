package org.acme.shortener.exception;

public class CodeGenerationException extends RuntimeException {
    public CodeGenerationException(String message) {
        super(message);
    }
}
