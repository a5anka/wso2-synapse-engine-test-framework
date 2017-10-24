package org.apache.synapse.integration.Tests;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.synapse.integration.BaseTest;
import org.apache.synapse.integration.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.protocol.emulator.dsl.Emulator;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientOperationBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientResponseProcessorContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.RequestResponseCorrelation;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ClientTest extends BaseTest {
    private String path = "/services/normal_server";
    private String responseBody = "{\"glossary\":{\"title\":\"exampleglossary\",\"GlossDiv\":{\"title\":\"S\"," +
            "\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\"," +
            "\"GlossTerm\":\"StandardGeneralizedMarkupLanguage\",\"Acronym\":\"SGML\"," +
            "\"Abbrev\":\"ISO8879:1986\",\"GlossDef\":{\"para\":\"Ameta-markuplanguage," +
            "usedtocreatemarkuplanguagessuchasDocBook.\",\"GlossSeeAlso\":[\"GML\"," +
            "\"XML\"]},\"GlossSee\":\"markup\"}}}}}";
    private File plainFile = new File("100KB.txt");
    private String processingPath = "/services/content_type";
    private String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<note>\n" +
            "  <to>Tove</to>\n" +
            "  <from>Jani</from>\n" +
            "  <heading>Reminder</heading>\n" +
            "  <body>Don't forget me this weekend!</body>\n" +
            "</note>";

    protected String getSynapseConfig() throws IOException {

        return TestUtils.getSynapseConfig("client.xml");
    }

    @Test
    public void testClientLargePayload() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(path)
                                .withMethod(HttpMethod.POST).withBody(new File("1MB.txt"))
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(responseBody, response.getReceivedResponseContext().getResponseBody());
        Assert.assertEquals(HttpHeaders.Values.APPLICATION_JSON,
                            response.getReceivedResponse().headers().get(HttpHeaders.Names.CONTENT_TYPE));
    }

    @org.testng.annotations.Test
    public void testClientProcessingLargePayload() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(processingPath)
                                .withMethod(HttpMethod.POST).withBody(new File("1MB.txt"))
                                .withHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain")
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(responseBody, response.getReceivedResponseContext().getResponseBody());
        Assert.assertEquals(HttpHeaders.Values.APPLICATION_JSON,
                            response.getReceivedResponse().headers().get(HttpHeaders.Names.CONTENT_TYPE));
    }

    @Test
    public void testClientSlowWriting() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort())).withWritingDelay(3000)
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(path)
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(responseBody, response.getReceivedResponseContext().getResponseBody());
        Assert.assertEquals(HttpHeaders.Values.APPLICATION_JSON,
                            response.getReceivedResponse().headers().get(HttpHeaders.Names.CONTENT_TYPE));
    }


    @Test
    public void testClientSlowReading() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort())).withReadingDelay(3000)
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(path)
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(responseBody, response.getReceivedResponseContext().getResponseBody());
        Assert.assertEquals(HttpHeaders.Values.APPLICATION_JSON,
                            response.getReceivedResponse().headers().get(HttpHeaders.Names.CONTENT_TYPE));
    }

    @Test
    public void testClientDisableKeepAlive() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort())).withKeepAlive(false)
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(path)
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(responseBody, response.getReceivedResponseContext().getResponseBody());
        Assert.assertEquals(HttpHeaders.Values.APPLICATION_JSON,
                            response.getReceivedResponse().headers().get(HttpHeaders.Names.CONTENT_TYPE));
        Assert.assertNotEquals(response.getReceivedResponse().headers().get(HttpHeaders.Names.CONNECTION),
                               HttpHeaders.Values.KEEP_ALIVE);
        Assert.assertEquals(response.getReceivedResponse().headers().get(HttpHeaders.Names.CONNECTION),
                            HttpHeaders.Values.CLOSE);
    }

    @Test
    public void testDisableChunking() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(path)
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(responseBody, response.getReceivedResponseContext().getResponseBody());
        Assert.assertEquals(HttpHeaders.Values.APPLICATION_JSON,
                            response.getReceivedResponse().headers().get(HttpHeaders.Names.CONTENT_TYPE));
    }

    @Test
    public void testDisconnectPartially() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                                .withPartialWriteConnectionDrop()
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(path)
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();

        Assert.assertNull(response);
    }

    @Test
    public void testBurstRequests() {
        for (int i = 0; i < 10; i++) {
            HttpClientOperationBuilderContext httpClientOperationBuilderContext = Emulator.getHttpEmulator()
                    .client()
                    .given(
                            HttpClientConfigBuilderContext.configure()
                                    .host(getConfig().getSynapseServer().getHostname())
                                    .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                    )
                    .when(
                            HttpClientRequestBuilderContext.request().withPath(path)
                                    .withMethod(HttpMethod.POST).withBody(plainFile)
                    )
                    .then(
                            HttpClientResponseBuilderContext.response().assertionIgnore()
                    )
                    .operation()
                    .sendAsync();
            List<RequestResponseCorrelation> responseCorrelations = httpClientOperationBuilderContext.shutdown();
            Assert.assertEquals(responseBody, responseCorrelations.get(0).getReceivedResponse()
                    .getReceivedResponseContext()
                    .getResponseBody());
            Assert.assertEquals(HttpHeaders.Values.APPLICATION_JSON, responseCorrelations.get(0).getReceivedResponse()
                    .getReceivedResponse()
                    .headers().get(HttpHeaders.Names.CONTENT_TYPE));
        }
    }

    @Test
    public void testMalformedPayload() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(processingPath)
                                .withMethod(HttpMethod.POST).withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                                              "<note>\n" +
                                                                              "  <to>Tove<to>\n" +
                                                                              "  <from>Jani</from>\n" +
                                                                              "  <heading>Reminder</heading>\n" +
                                                                              "  <body>Don't forget me this " +
                                                                              "weekend!</body>\n" +
                                                                              "</note>")
                                .withHeader(HttpHeaders.Names.CONTENT_TYPE, "application/xml")
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertNull(response.getReceivedResponseContext().getResponseBody());
        Assert.assertEquals(HttpResponseStatus.ACCEPTED, response.getReceivedResponseContext().getResponseStatus());
    }

    @Test
    public void testMissingHeaders() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(processingPath)
                                .withMethod(HttpMethod.POST).withBody(xmlBody)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertNull(response.getReceivedResponseContext().getResponseBody());
        Assert.assertEquals(HttpResponseStatus.ACCEPTED, response.getReceivedResponseContext().getResponseStatus());

    }

    @Test
    public void testInvalidHeader() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(processingPath).withMethod(HttpMethod.POST)
                                .withBody(plainFile)
                                .withHeader(HttpHeaders.Names.CONTENT_TYPE, "application/xml")
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertNull(response);
    }

    /**
     * TODO: Fix the emulator client code to get this working
     */
    @Test(enabled = false)
    public void testConnectionDropWhileReading() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                                .withEnableReadingConnectionDrop()
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath(path)
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertNull(response);
    }
}