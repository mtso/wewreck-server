package com.wepay.helpers;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Utilities {

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
}
