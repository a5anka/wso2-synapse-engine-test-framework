package org.apache.synapse.integration.utils;

import org.testng.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {
    public static String getCurrentDir() {
        return System.getProperty("user.dir") + File.separator;
    }

    /**
     * Get synapse configuration template file as string
     *
     * @param relativePath path relative to src/test/resources/synapseConfigs
     * @return string representation of the synapse config file content
     * @throws IOException if issue in accessing the file
     */
    public static String getSynapseConfig(String relativePath) throws IOException {
        String resourceLocation = getTestResourceLocation();

        String absolutePath = resourceLocation + File.separator + "synapseConfigs" + File.separator + relativePath;
        return getContentAsString(absolutePath);
    }

    /**
     * Get file content as string
     *
     * @param filePath path to file
     * @return string content
     * @throws IOException if issue accessing the file
     */
    public static String getContentAsString(String filePath) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(filePath));
        return new String(encoded, Charset.defaultCharset());
    }

    public static String getTestResourceLocation() {
        String resourceFileLocation = System.getProperty("framework.resource.location");

        //noinspection ObviousNullCheck System.getProperty can return null
        Assert.assertNotNull(resourceFileLocation, "framework.resource.location property should be set");

        return resourceFileLocation;
    }

    public static String getFileBody(File filePath) throws IOException {

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            int c;
            StringBuilder stringBuilder = new StringBuilder();
            while ((c = fileInputStream.read()) != -1) {
                stringBuilder.append(c);
            }
            String content = stringBuilder.toString();
            content = content.replace("\n", "").replace("\r", "");

            return content;
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }
}
