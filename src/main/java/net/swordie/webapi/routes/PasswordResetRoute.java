package net.swordie.webapi.routes;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.swordie.webapi.protocol.request.SetPasscodeRequest;

@Path("/reset-password")
public class PasswordResetRoute {

    @POST
    @Path("")
    @Consumes({MediaType.APPLICATION_JSON})
    public void setPassword(SetPasscodeRequest request) {
        throw new WebApplicationException("Password resets are unavailable.", Response.Status.GONE);
    }
}
