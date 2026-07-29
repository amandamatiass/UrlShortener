package org.acme.shortener;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/")
public class HomeResource {

    @ConfigProperty(name = "app.frontend-url", defaultValue = "https://url-shortener-eight-sandy.vercel.app")
    String frontendUrl;

    @GET
    public Response home() {
        return Response.status(Response.Status.FOUND).header("Location", frontendUrl).build();
    }
}