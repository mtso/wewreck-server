package com.wepay.resource;

import com.wepay.helpers.Utilities;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("legal_entities")
public class LegalEntitiesResource {
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response legalEntityLookUp(@PathParam("id") final String legalEntityId) {
        return Response.ok().entity(Utilities.legalEntityLookUp(legalEntityId)).build();
    }
}
