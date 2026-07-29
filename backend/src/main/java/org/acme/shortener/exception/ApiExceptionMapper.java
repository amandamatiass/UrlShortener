package org.acme.shortener.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Map;

public class ApiExceptionMapper {

    @ServerExceptionMapper
    public RestResponse<Map<String, String>> mapNotFound(CodeNotFoundException e) {
        return RestResponse.status(Response.Status.NOT_FOUND, Map.of("error", e.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<Map<String, String>> mapConflict(CodeAlreadyInUseException e) {
        return RestResponse.status(Response.Status.CONFLICT, Map.of("error", e.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<Map<String, String>> mapGenerationFailure(CodeGenerationException e) {
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, Map.of("error", e.getMessage()));
    }

    @ServerExceptionMapper
    public RestResponse<Map<String, String>> mapValidation(jakarta.validation.ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage())
                .orElse("Payload inválido");
        return RestResponse.status(Response.Status.BAD_REQUEST, Map.of("error", msg));
    }
}
