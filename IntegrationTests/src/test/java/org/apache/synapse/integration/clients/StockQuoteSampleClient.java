package org.apache.synapse.integration.clients;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.apache.synapse.integration.config.AutomationYamlFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

public class StockQuoteSampleClient {

    private final AutomationYamlFile.Axis2ClientConfig clientConfig;
    private ConfigurationContext configContext;
    private OMElement payload;
    private ServiceClient serviceClient;

    public StockQuoteSampleClient(AutomationYamlFile.Axis2ClientConfig configurations) {
        clientConfig = configurations;
    }

    public SampleClientResult requestStandardQuote(String addUrl, String trpUrl, String prxUrl, String symbol,
            String svcPolicy) {
        System.out.println("sending standard quote request");
        SampleClientResult clientResult = new SampleClientResult();
        try {
            init(addUrl, trpUrl, prxUrl, svcPolicy, 10000);

            payload = createStandardQuoteRequest(symbol, 1);
            serviceClient.getOptions().setAction("urn:getQuote");
            OMElement resultElement = serviceClient.sendReceive(payload);
            System.out.println("Standard :: Stock price = $" + parseStandardQuoteResponse(resultElement));
            clientResult.incrementResponseCount();
        } catch (Exception e) {
            System.out.println("Error invoking service");
            e.printStackTrace();
            clientResult.setException(e);
        } finally {
            terminate();
        }
        return clientResult;
    }

    private void terminate() {
        if (serviceClient != null) {
            try {
                serviceClient.cleanupTransport();
                serviceClient.cleanup();
                configContext.terminate();
            } catch (AxisFault axisFault) {
                System.out.println("Error terminating client");
                axisFault.printStackTrace();
            }
        }
    }

    private String parseStandardQuoteResponse(OMElement result) throws Exception {
        AXIOMXPath xPath = new AXIOMXPath("//ns:last");
        xPath.addNamespace("ns","http://services.samples/xsd");
        OMElement last = (OMElement) xPath.selectSingleNode(result);
        if (last != null) {
            return last.getText();
        } else {
            throw new Exception("Unexpected response : " + result);
        }
    }

    private OMElement createStandardQuoteRequest(String symbol, int itrCount) {
        OMFactory factory   = OMAbstractFactory.getOMFactory();
        OMNamespace ns      = factory.createOMNamespace("http://services.samples", "m0");
        OMElement getQuote  = factory.createOMElement("getQuote", ns);
        for (int i =0; i<itrCount; i++) {
            OMElement request   = factory.createOMElement("request", ns);
            OMElement symb      = factory.createOMElement("symbol", ns);
            request.addChild(symb);
            getQuote.addChild(request);
            symb.setText(symbol);
        }
        return getQuote;
    }

    private void init(String addUrl, String trpUrl, String prxUrl, String policyKey, int timeout)
            throws AxisFault, MalformedURLException, FileNotFoundException {
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                clientConfig.getClientRepo(),
                clientConfig.getAxis2Xml());
        serviceClient = new ServiceClient(configContext, null);

        Options options = new Options();
        if (addUrl != null && !"".equals(addUrl)) {
            serviceClient.engageModule("addressing");
            options.setTo(new EndpointReference(addUrl));
        }

        if (trpUrl != null && !"".equals(trpUrl)) {
            options.setProperty(Constants.Configuration.TRANSPORT_URL, trpUrl);
        }

        if (prxUrl != null && !"".equals(prxUrl)) {
            HttpTransportProperties.ProxyProperties proxyProperties =
                    new HttpTransportProperties.ProxyProperties();
            URL url = new URL(prxUrl);
            proxyProperties.setProxyName(url.getHost());
            proxyProperties.setProxyPort(url.getPort());
            proxyProperties.setUserName("");
            proxyProperties.setPassWord("");
            proxyProperties.setDomain("");
            options.setProperty(HTTPConstants.PROXY, proxyProperties);
        }

        if (policyKey != null && !"".equals(policyKey)) {
            System.out.println("Using WS-Security");
            serviceClient.engageModule("addressing");
            serviceClient.engageModule("rampart");
            OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(new FileInputStream(policyKey));
            Policy policy = PolicyEngine.getPolicy(builder.getDocumentElement());
            options.setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
        }

        if (timeout > 0) {
            System.out.println("setting client timeout to: " + timeout);
            options.setTimeOutInMilliSeconds(timeout);
        }

        serviceClient.setOptions(options);
    }
}
