package com.wepay.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wepay.helpers.Utilities;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("webhooks")
public class WebHooks {

    private static Logger log = LoggerFactory.getLogger(WebHooks.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private final WebTarget webTargetBeaver;

    public WebHooks() {
        final ClientConfig configuration = new ClientConfig();
        configuration.property(
                ClientProperties.CONNECT_TIMEOUT,
                5000);
        configuration.property(
                ClientProperties.READ_TIMEOUT,
                3000);
        this.webTargetBeaver =
                ClientBuilder
                        .newClient(configuration)
                        .target("https://stage-api.wepay.com");
    }

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
            ObjectNode geo = this.getGeoLocationForPaymentMethod(paymentMethodId);

            record.set("payment", paymentAPIRecord);
            record.set("topic", paymentObj.path("topic"));
            record.set("geo", geo);

            PaymentsEndpoint.onPaymentCreated(accountId, record.toString());
        } catch (IOException e) {
            log.error(e.toString());
            return Response.serverError().build();
        }
        return Response.ok().entity(record.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    private ObjectNode getGeoLocationForPaymentMethod(String paymentMethodId) throws IOException {
        final Response response =
                this.webTargetBeaver
                    .path("payment_methods/" + paymentMethodId)
                    .request()
                    .header("Api-Version", "3.0-alpha.1")
                    .header("App-Id", "87571")
                    .header("App-Token", "7c4fa53c09")
                    .get();
        ObjectNode respObj = (ObjectNode) mapper.readTree(response.readEntity(String.class));
        //log.info("Payment method lookup from Beaver: " + respObj.toString());
        String type = respObj.path("type").asText();
        String zip;
        switch (type) {
            case "credit_card":
                zip = respObj.path(type).path("card_holder").path("address").path("postal_code").asText();
                break;
            case "payment_bank_us":
                zip = respObj.path(type).path("account_holder").path("address").path("postal_code").asText();
                break;
            default:
                zip = "94063";
        }
        return Utilities.getLatLngForZip(zip);
    }
}
