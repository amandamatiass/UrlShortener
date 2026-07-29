package org.acme.shortener.exception;

public class CodeNotFoundException extends RuntimeException {
    public CodeNotFoundException(String code) {
        super("Nenhuma URL encontrada para o código '" + code + "'");
    }
}
