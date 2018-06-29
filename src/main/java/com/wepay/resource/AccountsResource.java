package com.wepay.resource;

import com.wepay.helpers.Utilities;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("accounts")
public class AccountsResource {

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response accountLookUp(@PathParam("id") final String accountId) {
        return Response.ok().entity(Utilities.accountLookUp(accountId)).build();
    }

    @GET
    @Path("{id}/capabilities")
    @Produces(MediaType.APPLICATION_JSON)
    public Response accountCapabilitiesLookUp(@PathParam("id") final String accountId) {
        return Response.ok().entity(Utilities.accountCapabilitiesLookUp(accountId)).build();
    }
}
