package com.wepay.helpers;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Utilities {

    private static Map<String, ObjectNode> zips;
    private static Logger log = LoggerFactory.getLogger(Utilities.class);
    private static ObjectNode wepayHQ;

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

    public static ObjectNode getLatLngForZip(String zip) {
        return zips.getOrDefault(zip, wepayHQ);
    }
}
