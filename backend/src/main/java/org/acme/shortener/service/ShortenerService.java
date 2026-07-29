package org.acme.shortener.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.shortener.UrlMapping;
import org.acme.shortener.dto.ShortenRequest;
import org.acme.shortener.dto.ShortenResponse;
import org.acme.shortener.exception.CodeAlreadyInUseException;
import org.acme.shortener.exception.CodeGenerationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class ShortenerService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7;
    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();

    @Inject
    UrlRepository repository;

    @ConfigProperty(name = "app.base-url", defaultValue = "https://short.example.com")
    String baseUrl;

    public ShortenResponse shorten(ShortenRequest request) {
        String code = (request.customCode != null && !request.customCode.isBlank())
                ? request.customCode.trim()
                : null;

        UrlMapping mapping = new UrlMapping();
        mapping.setUrl(request.url);
        mapping.setCreatedAt(Instant.now().getEpochSecond());
        mapping.setClicks(0);

        if (code != null) {
            mapping.setCode(code);
            boolean saved = repository.saveIfAbsent(mapping);
            if (!saved) {
                throw new CodeAlreadyInUseException(code);
            }
            return toResponse(mapping);
        }

        // Gera código aleatório, tentando novamente em caso de colisão rara.
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String generated = generateCode();
            mapping.setCode(generated);
            if (repository.saveIfAbsent(mapping)) {
                return toResponse(mapping);
            }
        }
        throw new CodeGenerationException(
                "Não foi possível gerar um código único após " + MAX_ATTEMPTS + " tentativas");
    }

    public Optional<String> resolve(String code) {
        Optional<UrlMapping> mapping = repository.findByCode(code);
        mapping.ifPresent(repository::incrementClicks);
        return mapping.map(UrlMapping::getUrl);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private ShortenResponse toResponse(UrlMapping mapping) {
        return new ShortenResponse(
                mapping.getCode(),
                baseUrl + "/" + mapping.getCode(),
                mapping.getUrl(),
                mapping.getCreatedAt());
    }
}
