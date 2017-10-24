package org.apache.synapse.integration;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class TestSample extends BaseTest {

    protected String getSynapseConfig() {
        return "<definitions xmlns=\"http://ws.apache.org/ns/synapse\">\n"
                + "\n"
                + "    <sequence name=\"main\">\n"
                + "        <!-- ${good.backend} log all attributes of messages passing through -->\n"
                + "        <log level=\"full\"/>\n"
                + "    \n"
                + "        <!-- Send the message to implicit destination -->\n"
                + "        <send/>\n"
                + "    </sequence>\n"
                + "\n"
                + "</definitions>\n";
    }

    /**
     * Sample test case description
     *
     * @throws Exception due to error
     */
    @Test(enabled = false)
    public void testSampleMethod() throws Exception {

        // Test logic goes here
        TimeUnit.SECONDS.sleep(10);
    }
}
