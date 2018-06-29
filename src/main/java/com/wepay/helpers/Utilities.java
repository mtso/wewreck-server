package com.wepay.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Utilities {

    private static Map<String, ObjectNode> zips;
    private static Logger log = LoggerFactory.getLogger(Utilities.class);
    private static ObjectNode wepayHQ;
    private static ObjectMapper mapper = new ObjectMapper();
    private static WebTarget webTargetBeaver;
    private static MultivaluedMap<String, Object> headers;

    static {
        zips = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(Utilities.class.getClass().getResourceAsStream("/" + "zip_lat_lng.csv")));
            String line;

            while((line=br.readLine())!=null){
                String str[] = line.split(",");
                ObjectNode latLng = JsonNodeFactory.instance.objectNode();
                latLng.put("lat", str[1].trim());
                latLng.put("lng", str[2].trim());
                zips.put(str[0], latLng);
            }
        } catch (IOException e) {
            log.error(e.toString());
        }
        log.info("loaded " + zips.size() + " zips!!");
        wepayHQ = JsonNodeFactory.instance.objectNode();
        wepayHQ.put("lat", "37.493268");
        wepayHQ.put("lng", "-122.195281");

        final ClientConfig configuration = new ClientConfig();
        configuration.property(
                ClientProperties.CONNECT_TIMEOUT,
                5000);
        configuration.property(
                ClientProperties.READ_TIMEOUT,
                3000);
        webTargetBeaver =
                ClientBuilder
                        .newClient(configuration)
                        .target("https://stage-api.wepay.com");

        headers = new MultivaluedHashMap<>();
        headers.put("Api-Version", Collections.singletonList("3.0-alpha.1"));
        headers.put("App-Id", Collections.singletonList("87571"));
        headers.put("App-Token", Collections.singletonList("7c4fa53c09"));
    }

    /**
     * Consumes a FileInputStream and returns the string value.
     *
     * @param inputStream - the FileInputStream to consume
     * @return - the string value of the file's contents
     * @throws IOException if unable to read file
     */
    public static String getStringFromFileInputStream(@NotNull InputStream inputStream) throws IOException {
        // Claimed to be teh fastest approach for large & small streams:
        // https://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    public static ObjectNode getGeoLocationForPaymentMethod(String paymentMethodId) throws IOException {
        final Response response =
                webTargetBeaver
                    .path("payment_methods/" + paymentMethodId)
                    .request()
                    .headers(headers)
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
        ObjectNode geo =  Utilities.getLatLngForZip(zip);
        geo.put("zip", zip);
        return geo;
    }

    public static String accountLookUp(String accountId) {
        final Response response =
                webTargetBeaver
                        .path("accounts/" + accountId)
                        .request()
                        .headers(headers)
                        .get();
        return response.readEntity(String.class);
    }

    public static String accountCapabilitiesLookUp(String accountId) {
        final Response response =
                webTargetBeaver
                        .path("accounts/" + accountId + "/capabilities")
                        .queryParam("is_expanded", "true")
                        .request()
                        .headers(headers)
                        .get();
        return response.readEntity(String.class);
    }

    public static String legalEntityLookUp(String legalEntityId) {
        final Response response =
                webTargetBeaver
                        .path("legal_entities/" + legalEntityId)
                        .request()
                        .headers(headers)
                        .get();
        return response.readEntity(String.class);
    }

    private static ObjectNode getLatLngForZip(String zip) {
        return zips.getOrDefault(zip, wepayHQ);
    }
}
