package org.apache.synapse.integration.Tests;

import org.apache.synapse.integration.BaseTest;
import org.apache.synapse.integration.utils.TestUtils;

import java.io.IOException;

public class Common extends BaseTest{
    protected String getSynapseConfig() throws IOException {

        return TestUtils.getSynapseConfig("common.xml");
    }
}
