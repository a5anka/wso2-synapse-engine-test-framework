package org.apache.synapse.integration.utils;

import org.junit.Assert;

import java.io.File;
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

        byte[] encoded = Files.readAllBytes(
                Paths.get(resourceLocation + File.separator + "synapseConfigs" + File.separator + relativePath));

        return new String(encoded, Charset.defaultCharset());
    }

    public static String getTestResourceLocation() {
        String resourceFileLocation = System.getProperty("framework.resource.location");

        //noinspection ObviousNullCheck System.getProperty can return null
        Assert.assertNotNull(resourceFileLocation, "framework.resource.location property should be set");

        return resourceFileLocation;
    }
}
