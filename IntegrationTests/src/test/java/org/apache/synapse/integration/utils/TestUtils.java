package org.apache.synapse.integration.utils;

import java.io.File;

public class TestUtils {
    public static String getCurrentDir() {
        return System.getProperty("user.dir") + File.separator;
    }
}
