package com.wepay.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wepay.helpers.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("webhooks")
public class WebHooks {

    private static Logger log = LoggerFactory.getLogger(WebHooks.class);
    private static ObjectMapper mapper = new ObjectMapper();

    @POST
    @Path("payments")
    public Response payments(final ContainerRequestContext requestContext) {
        final String json;
        final ObjectNode record = JsonNodeFactory.instance.objectNode(); // Containing the payment api record, and the geo location
        final ObjectNode paymentObj;
        try {
            json = Utilities.getStringFromFileInputStream(requestContext.getEntityStream());
            paymentObj = (ObjectNode) mapper.readTree(json);
            record.set("payment", paymentObj.path("payload"));
        } catch (IOException e) {
            log.error(e.toString());
            return Response.serverError().build();
        }
        return Response.ok().build();
    }
}
