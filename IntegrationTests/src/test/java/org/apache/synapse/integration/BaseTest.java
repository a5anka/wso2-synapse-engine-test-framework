package org.apache.synapse.integration;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.synapse.integration.clients.StockQuoteSampleClient;
import org.apache.synapse.integration.config.AutomationYamlFile;
import org.apache.synapse.integration.utils.TestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public abstract class BaseTest {

    private static AutomationYamlFile configurations;
    private static StrSubstitutor strSubstitutor;

    @BeforeClass
    public static void initParameters() {
        String resourceFileLocation = TestUtils.getTestResourceLocation();

        FileInputStream yamlInput = null;
        try {
            yamlInput = new FileInputStream(new File(
                    resourceFileLocation + File.separator + "automation.yaml"));
        } catch (FileNotFoundException e) {
            Assert.fail(e.getMessage());
        }

        Yaml yaml = new Yaml();
        configurations = yaml.loadAs(yamlInput, AutomationYamlFile.class);

        Properties endpointProperties = new Properties();
        try {
            endpointProperties.load(new FileInputStream(resourceFileLocation + File.separator + "endpoint.properties"));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        HashMap<String, String> valuesMap = new HashMap<>();
        for (Map.Entry<Object, Object> objectObjectEntry : endpointProperties.entrySet()) {
            valuesMap.put((String) objectObjectEntry.getKey(), (String) objectObjectEntry.getValue());
        }
        strSubstitutor = new StrSubstitutor(valuesMap);
    }

    protected abstract String getSynapseConfig() throws IOException;

    @Before
    public void setup() throws Exception {
        uploadConfig();
        startRemoteSynapseServer();
    }

    @After
    public void teardown() throws IOException {
        stopRemoteSynapseServer();
    }

    public void uploadConfig() throws IOException {
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod("http://" + getSynapseAgentAddress() + "/synapseAgent/upload-config");

        StringRequestEntity test_content = new StringRequestEntity(strSubstitutor.replace(getSynapseConfig()),
                                                                   "application/x-www-form-urlencoded", null);

        method.setRequestEntity(test_content);

        int statusCode = client.executeMethod(method);
    }

    public AutomationYamlFile getConfig() {
        return configurations;
    }

    public String getSynapseAddress() {
        return getConfig().getSynapseServer().getHostAddress();
    }

    public String getBackendAddress() {
        return getConfig().getBackendServer().getHostAddress();
    }

    public StockQuoteSampleClient getStockQuoteClient() {
        return new StockQuoteSampleClient(configurations.getAxis2ClientConfig());
    }

    public String getSynapseAgentAddress() {
        return getConfig().getSynapseAgent().getHostAddress();
    }

    private void startRemoteSynapseServer() throws Exception {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod("http://" + getSynapseAgentAddress() + "/synapseAgent/start");

        int statusCode = client.executeMethod(method);

        System.out.println("Status : " + statusCode);

        String synapseHostname = getConfig().getSynapseServer().getHostname();
        int synapsePort = Integer.parseInt(getConfig().getSynapseServer().getPort());

        boolean available = isPortAvailable(synapseHostname, synapsePort);
        int tries = 1;

        while (!available && tries < 10) {
            TimeUnit.SECONDS.sleep(1);
            available = isPortAvailable(synapseHostname, synapsePort);
            tries++;
        }
    }

    private boolean isPortAvailable(String synapseHostname, int synapsePort) {
        try {
            // This will return when we can successfully connect to the port
            new Socket(synapseHostname, synapsePort).close();
            System.out.println("Port available");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void stopRemoteSynapseServer() throws Exception {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod("http://" + getSynapseAgentAddress() + "/synapseAgent/stop");

        int statusCode = client.executeMethod(method);

        System.out.println("Status : " + statusCode);

        String synapseHostname = getConfig().getSynapseServer().getHostname();
        int synapsePort = Integer.parseInt(getConfig().getSynapseServer().getPort());

        boolean available = isPortAvailable(synapseHostname, synapsePort);
        int tries = 1;

        while (available && tries < 10) {
            TimeUnit.SECONDS.sleep(1);
            available = isPortAvailable(synapseHostname, synapsePort);
            tries++;
        }
    }
}
