package org.acme.shortener;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.shortener.dto.ShortenRequest;
import org.acme.shortener.dto.ShortenResponse;
import org.acme.shortener.exception.CodeNotFoundException;
import org.acme.shortener.service.ShortenerService;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShortenerResource {

    @Inject
    ShortenerService service;

    @POST
    @Path("/shorten")
    public Response shorten(@Valid ShortenRequest request) {
        ShortenResponse response = service.shorten(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{code}")
    public Response getMetadata(@PathParam("code") String code) {
        String url = service.resolve(code).orElseThrow(() -> new CodeNotFoundException(code));
        return Response.ok(new ShortenResponse(code, null, url, 0)).build();
    }
}
