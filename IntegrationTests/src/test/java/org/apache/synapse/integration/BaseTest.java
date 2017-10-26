package org.apache.synapse.integration;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.integration.clients.StockQuoteSampleClient;
import org.apache.synapse.integration.config.AutomationYamlFile;
import org.apache.synapse.integration.utils.TestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public abstract class BaseTest {

    private static final Log BASE_LOGGER = LogFactory.getLog(BaseTest.class);
    private static AutomationYamlFile configurations;
    private static StrSubstitutor strSubstitutor;

    @BeforeClass
    public void initParameters() throws Exception {
        String resourceFileLocation = TestUtils.getTestResourceLocation();

        FileInputStream yamlInput;

        yamlInput = new FileInputStream(new File(resourceFileLocation + File.separator + "automation.yaml"));

        Yaml yaml = new Yaml();
        configurations = yaml.loadAs(yamlInput, AutomationYamlFile.class);

        Properties endpointProperties = new Properties();
        FileInputStream propertiesInput = new FileInputStream(
                resourceFileLocation + File.separator + "endpoint.properties");
        endpointProperties.load(propertiesInput);

        HashMap<String, String> valuesMap = new HashMap<>();
        for (Map.Entry<Object, Object> objectObjectEntry : endpointProperties.entrySet()) {
            valuesMap.put((String) objectObjectEntry.getKey(), (String) objectObjectEntry.getValue());
        }
        strSubstitutor = new StrSubstitutor(valuesMap);

        yamlInput.close();
        propertiesInput.close();

        uploadConfig();
    }

    protected abstract String getSynapseConfig() throws IOException;

    @BeforeMethod
    public void setup(Method method) throws Exception {
        startRemoteSynapseServer();
    }

    @AfterMethod
    public void teardown(Method method) throws Exception {
        stopRemoteSynapseServer();
    }

    @AfterClass
    public void teardownClass() throws Exception {
        // Do nothing
    }

    public void uploadConfig() throws IOException {
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod("http://" + getSynapseAgentAddress() + "/synapseAgent/upload-config");

        StringRequestEntity test_content = new StringRequestEntity(strSubstitutor.replace(getSynapseConfig()),
                                                                   "application/x-www-form-urlencoded", null);

        method.setRequestEntity(test_content);

        client.executeMethod(method);
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

        client.executeMethod(method);

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
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void stopRemoteSynapseServer() throws Exception {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod("http://" + getSynapseAgentAddress() + "/synapseAgent/stop");

        client.executeMethod(method);

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
