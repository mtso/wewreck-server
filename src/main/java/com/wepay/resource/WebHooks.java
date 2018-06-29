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
import javax.ws.rs.core.MediaType;
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
        // The Record contains the payment api record, the geo location and the topic
        // It's sent to the web sockets
        final ObjectNode record = JsonNodeFactory.instance.objectNode();
        final ObjectNode paymentObj;
        try {
            json = Utilities.getStringFromFileInputStream(requestContext.getEntityStream());
            paymentObj = (ObjectNode) mapper.readTree(json);
            ObjectNode paymentAPIRecord = (ObjectNode) paymentObj.path("payload");

            String accountId = paymentAPIRecord.path("owner").path("id").asText();
            String paymentMethodId = paymentAPIRecord.path("payment_method").path("id").asText();
            ObjectNode geo = Utilities.getGeoLocationForPaymentMethod(paymentMethodId);

            record.set("payment", paymentAPIRecord);
            record.set("topic", paymentObj.path("topic"));
            record.set("geo", geo);
            log.info("Received payment event for: " + paymentAPIRecord.path("id").asText() + " geo info: " + geo.toString());

            PaymentsEndpoint.onPaymentCreated(accountId, record.toString());
        } catch (IOException e) {
            log.error(e.toString());
            return Response.serverError().build();
        }
        return Response.ok().entity(record.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
