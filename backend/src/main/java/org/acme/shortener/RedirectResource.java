package org.acme.shortener;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.acme.shortener.exception.CodeNotFoundException;
import org.acme.shortener.service.ShortenerService;

@Path("/{code}")
public class RedirectResource {

    @Inject
    ShortenerService service;

    @GET
    public Response redirect(@PathParam("code") String code) {
        String url = service.resolve(code).orElseThrow(() -> new CodeNotFoundException(code));
        return Response.status(Response.Status.FOUND).header("Location", url).build();
    }
}
