package com.wepay.resource;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("webhooks")
public class WebHooks {

    @POST
    @Path("payments")
    public Response payments() {
        return Response.ok().build();
    }
}
