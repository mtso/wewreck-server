package com.wepay.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

@Path("webhooks")
public class WebHooks {

    private static Logger log = LoggerFactory.getLogger(WebHooks.class);

    @POST
    @Path("payments")
    public Response payments(final ContainerRequestContext requestContext) {
        String json = requestContext.getEntityStream().toString();
        log.info(json);
        return Response.ok().entity(json).build();
    }
}
