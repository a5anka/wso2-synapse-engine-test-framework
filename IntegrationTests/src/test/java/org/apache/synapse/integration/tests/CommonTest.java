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
import org.apache.synapse.integration.BaseTest;
import org.apache.synapse.integration.utils.ServerConstants;
import org.apache.synapse.integration.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.protocol.emulator.dsl.Emulator;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientOperationBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientResponseProcessorContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpResponseContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.RequestResponseCorrelation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommonTest extends BaseTest {
    protected String getSynapseConfig() throws IOException {

        return TestUtils.getSynapseConfig("common.xml");
    }

    private File largeFile = new File("src/test/resources/files/1MB.txt");
    private File plainFile = new File("src/test/resources/files/100KB.txt");

    @Test
    public void testLargePayloadClientServer() throws IOException {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/large_payload")
                                .withMethod(HttpMethod.POST).withBody(largeFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(TestUtils.getFileBody(largeFile), response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
    }

    @Test
    public void testBackendLargeSlowClient() throws IOException {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort())).withReadingDelay(3000)
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/large_payload")
                                .withMethod(HttpMethod.POST).withBody(largeFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(TestUtils.getFileBody(largeFile), response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
    }

    @Test
    public void testLargeFileClientSlowReadingBackend() throws Exception {
        String payload = TestUtils.getContentAsString("src/test/resources/files/1MB.txt");
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/reading_delay")
                                .withMethod(HttpMethod.POST).withBody(payload)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(),
                            "Slowly reading backend",
                            "Slowly reading backend response did not receive correctly");
    }

    @Test
    public void testSlowWritingLargeResponseBackend() throws IOException {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/slow_large")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(TestUtils.getFileBody(largeFile), response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
    }

    @Test
    public void testClientSlowWritingLargePayload() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort())).withWritingDelay(3000)
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/normal_server")
                                .withMethod(HttpMethod.POST).withBody(largeFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(),
                            "{\"glossary\":{\"title\":\"exampleglossary\",\"GlossDiv\":{\"title\":\"S\"," +
                                    "\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\"," +
                                    "\"GlossTerm\":\"StandardGeneralizedMarkupLanguage\",\"Acronym\":\"SGML\"," +
                                    "\"Abbrev\":\"ISO8879:1986\",\"GlossDef\":{\"para\":\"Ameta-markuplanguage," +
                                    "usedtocreatemarkuplanguagessuchasDocBook.\",\"GlossSeeAlso\":[\"GML\"," +
                                    "\"XML\"]},\"GlossSee\":\"markup\"}}}}}",
                            "The received response body is not same as the expected");
        Assert.assertEquals(response.getReceivedResponse().headers().get(HttpHeaders.Names.CONTENT_TYPE),
                            HttpHeaders.Values.APPLICATION_JSON,
                            "The received ContentType header is different from that expected");
    }

    @Test
    public void testClientSlowWritingServerSlowReading() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort())).withWritingDelay(3000)
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/reading_delay")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Slowly reading backend",
                            "The received response body is not same as the expected");
    }

    @Test
    public void testClientSlowWritingBackendLargeResponseSlowWriting() throws IOException {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort())).withWritingDelay(3000)
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/slow_large")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(TestUtils.getFileBody(largeFile), response.getReceivedResponseContext().getResponseBody(),
                            "The received response body is not same as the expected");
    }

    @Test
    public void testClientSlowReadingServerSlowReading() {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator()
                .client()
                .given(
                        HttpClientConfigBuilderContext.configure()
                                .host(getConfig().getSynapseServer().getHostname())
                                .port(Integer.parseInt(getConfig().getSynapseServer().getPort())).withReadingDelay(3000)
                )
                .when(
                        HttpClientRequestBuilderContext.request().withPath("/services/reading_delay")
                                .withMethod(HttpMethod.POST).withBody(plainFile)
                )
                .then(
                        HttpClientResponseBuilderContext.response().assertionIgnore()
                )
                .operation()
                .send();
        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), "Slowly reading backend",
                            "The received response body is not same as the expected");
    }

    @Test
    public void testBurstRequestsWithKeepAlive() throws Exception {
        String payload = TestUtils.getContentAsString("src/test/resources/files/100KB.xml");
        int numberOfRequests = 100;
        List<HttpClientOperationBuilderContext> contextList = new ArrayList<>(numberOfRequests);

        for (int i = 0; i < numberOfRequests; i++) {
            HttpClientOperationBuilderContext httpClientOperationBuilderContext = Emulator
                    .getHttpEmulator()
                    .client()
                    .given(HttpClientConfigBuilderContext
                                   .configure()
                                   .host(getConfig().getSynapseServer().getHostname())
                                   .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                                   .withKeepAlive(true)
                    )
                    .when(HttpClientRequestBuilderContext
                                  .request()
                                  .withPath("/services/normal_server")
                                  .withMethod(HttpMethod.POST)
                                  .withBody(payload)
                    )
                    .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                    .operation()
                    .sendAsync();
            contextList.add(httpClientOperationBuilderContext);
        }

        for (int i = 0; i < numberOfRequests; i++) {
            HttpClientOperationBuilderContext context = contextList.get(i);
            List<RequestResponseCorrelation> responseCorrelations = context.shutdown();
            HttpResponseContext responseContext = responseCorrelations.get(0).getReceivedResponse()
                    .getReceivedResponseContext();
            Assert.assertEquals(ServerConstants.GOOD_SERVER_JSON_RESPONS, responseContext.getResponseBody(),
                                "The received response body is not same as the expected");
            Assert.assertEquals(HttpHeaders.Values.APPLICATION_JSON,
                                responseContext.getHeaderParameters().get(HttpHeaders.Names.CONTENT_TYPE).get(0),
                                "The received ContentType header value is different from that expected");
        }
    }
}
