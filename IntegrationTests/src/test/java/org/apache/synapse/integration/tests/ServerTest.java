/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.integration.tests;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.synapse.integration.BaseTest;
import org.apache.synapse.integration.utils.ServerConstants;
import org.apache.synapse.integration.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.protocol.emulator.dsl.Emulator;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientResponseProcessorContext;

import java.io.File;
import java.io.IOException;

public class ServerTest extends BaseTest {
    private static final String XML_FILE_100KB = "src/test/resources/files/100KB.xml";

    private File plainFile = new File("src/test/resources/files/100KB.txt");

    @Test
    public void testLargePayload() throws IOException {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/large_payload")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(TestUtils.getFileBody(new File("src/test/resources/files/1MB.txt")),
                            response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
    }

    @Test
    public void testSlowResponse() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/slow_response")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals("Slowly responding backend", response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
    }

    @Test
    public void testReadingDelay() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/reading_delay")
                                .withMethod(HttpMethod.POST)
                                .withBody("A small body to avoid timeout in the synapse server")
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals("Slowly reading backend", response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
    }

    @Test
    public void testWritingDelay() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/writing_delay")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals("Slowly writing backend", response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
    }

    @Test
    public void testKeepAlive() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/keep_alive")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals("Keep alive", response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
    }

    /**
     * Enable after fixing https://github.com/wso2/product-ei/issues/1211
     *
     * @throws Exception
     */
    @Test(enabled = false)
    public void testChunkingDisabled() throws Exception {
        String xmlData = TestUtils.getContentAsString(XML_FILE_100KB);

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/chunking_disabled")
                                .withMethod(HttpMethod.POST)
                                .withBody(xmlData)
                                .withHeader(HttpHeaders.Names.CONTENT_TYPE, "application/xml")
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();

        Assert.assertEquals("<Exception>Error in proxy execution</Exception>",
                            response.getReceivedResponseContext().getResponseBody().trim(),
                            "Did not receive an error message due to chunking disabled backend error");
        Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                            response.getReceivedResponseContext().getResponseStatus(),
                            "Status code should be 500 for errors");
    }

    @Test
    public void testChunkingDisabledSynapse() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/chunking_disabled_synapse")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                                .withHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain")
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals("Chunking disabled", response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
    }

    @Test
    public void testHttp10NotSupported() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/support_http10")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals("The HTTP/1.1 is not supported because of the configurations\n",
                            response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
        Assert.assertEquals(HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED, response.getReceivedResponse().getStatus(),
                            "The received response status is not same as the expected");
    }

    /**
     * Enable after fixing https://github.com/wso2/product-ei/issues/1213
     */
    @Test(enabled = false)
    public void testMalformedPayload() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/malformed_payload")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals("<Exception>Error in proxy execution</Exception>",
                            response.getReceivedResponseContext().getResponseBody().trim(),
                            "Did not receive an error message when payload is malformed payload");
        Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                            response.getReceivedResponseContext().getResponseStatus(),
                            "Status code should be 500 for malformed payload");
    }

    @Test
    public void testRandomDrop() {
        Emulator.getHttpEmulator().client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/random_drop")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/normal_server")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(ServerConstants.GOOD_SERVER_JSON_RESPONS,
                            response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
        Assert.assertEquals(response.getReceivedResponse().headers().get(HttpHeaders.Names.CONTENT_TYPE),
                            HttpHeaders.Values.APPLICATION_JSON,
                            "The received ContentType header value is different from that expected");
    }

    @Test
    public void testMissingHeader() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/missing_header")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertNotEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                       "<note>\n" +
                                       "  <to>Tove</to>\n" +
                                       "  <from>Jani</from>\n" +
                                       "  <heading>Reminder</heading>\n" +
                                       "  <body>Don't forget me this weekend!</body>\n" +
                                       "</note>",
                               response.getReceivedResponseContext().getResponseBody());
        Assert.assertEquals("application/octet-stream; charset=UTF-8",
                            response.getReceivedResponse().headers().get(HttpHeaders.Names.CONTENT_TYPE));
    }

    /**
     * Enable after fixing https://github.com/wso2/product-ei/issues/1211
     *
     * @throws Exception
     */
    @Test(enabled = false)
    public void testReadingDrop() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/reading_delay_server")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody().trim(),
                            "<Exception>Error in proxy execution</Exception>",
                            "Did not receive an error message when request reading is interrupted");
        Assert.assertEquals(response.getReceivedResponseContext().getResponseStatus(),
                            HttpResponseStatus.INTERNAL_SERVER_ERROR,
                            "Status code should be 500 for server error");
    }

    @Override
    protected String getSynapseConfig() throws IOException {
        return TestUtils.getSynapseConfig("server.xml");

    }
}
