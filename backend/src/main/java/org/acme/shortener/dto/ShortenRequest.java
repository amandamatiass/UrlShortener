package org.acme.shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ShortenRequest {

    @NotBlank(message = "url é obrigatória")
    @Pattern(regexp = "^https?://.+", message = "url deve começar com http:// ou https://")
    public String url;

    // código customizado opcional (alias). Se nulo, é gerado automaticamente.
    public String customCode;

    public ShortenRequest() {
    }

    public ShortenRequest(String url, String customCode) {
        this.url = url;
        this.customCode = customCode;
    }
}
