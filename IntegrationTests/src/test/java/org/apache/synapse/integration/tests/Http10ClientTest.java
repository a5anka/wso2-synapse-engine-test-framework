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

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.synapse.integration.BaseTest;
import org.apache.synapse.integration.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.protocol.emulator.dsl.Emulator;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.carbon.protocol.emulator.http.client.contexts.HttpClientResponseProcessorContext;

import java.io.IOException;

public class Http10ClientTest extends BaseTest {

    @Override
    protected String getSynapseConfig() throws IOException {
        return TestUtils.getSynapseConfig("http10Client.xml");
    }

    /**
     * The client will use HTTP 1.0 while backend use HTTP 1.1 to communicate with synapse server.
     */
    @Test
    public void testHttp10ToHttp11() {
        HttpClientConfigBuilderContext clientConfig = HttpClientConfigBuilderContext
                .configure()
                .host(getConfig().getSynapseServer().getHostname())
                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                .withWritingDelay(3000);

        HttpClientRequestBuilderContext requestConfig = HttpClientRequestBuilderContext
                .request()
                .withPath("/services/normal_server")
                .withMethod(HttpMethod.POST)
                .withHttpVersion(HttpVersion.HTTP_1_0)
                .withXmlPayload(getXmlBody());

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client().given(clientConfig)
                                                              .when(requestConfig)
                                                              .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                                                              .operation().send();

        Assert.assertEquals(getResponseBody(), response.getReceivedResponseContext().getResponseBody());
    }


    /**
     * The client and backend will use HTTP 1.0 to communicate with synapse server.
     */
    @Test
    public void testHttp10ToHttp10() {
        HttpClientConfigBuilderContext clientConfig = HttpClientConfigBuilderContext
                .configure()
                .host(getConfig().getSynapseServer().getHostname())
                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                .withWritingDelay(3000);

        HttpClientRequestBuilderContext requestConfig = HttpClientRequestBuilderContext
                .request()
                .withPath("/services/http10_only")
                .withMethod(HttpMethod.POST)
                .withHttpVersion(HttpVersion.HTTP_1_0)
                .withXmlPayload(getXmlBody());

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client().given(clientConfig)
                                                              .when(requestConfig)
                                                              .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                                                              .operation().send();

        Assert.assertEquals("HTTP version 1.0 only", response.getReceivedResponseContext().getResponseBody().trim());
    }

    /**
     * The client will use HTTP 1.1 while the backend use HTTP 1.0 to communicate with synapse server.
     */
    @Test
    public void testHttp11ToHttp10() {
        HttpClientConfigBuilderContext clientConfig = HttpClientConfigBuilderContext
                .configure()
                .host(getConfig().getSynapseServer().getHostname())
                .port(Integer.parseInt(getConfig().getSynapseServer().getPort()))
                .withWritingDelay(3000);

        HttpClientRequestBuilderContext requestConfig = HttpClientRequestBuilderContext
                .request()
                .withPath("/services/http10_only")
                .withMethod(HttpMethod.POST)
                .withXmlPayload(getXmlBody());

        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client().given(clientConfig)
                                                              .when(requestConfig)
                                                              .then(HttpClientResponseBuilderContext.response().assertionIgnore())
                                                              .operation().send();

        Assert.assertEquals("HTTP version 1.0 only", response.getReceivedResponseContext().getResponseBody().trim());
    }


    private String getXmlBody() {
        return "<request>Http10ClientTest</request>";
    }

    private String getResponseBody() {
        return "{\"glossary\":{\"title" +
                "\":\"exampleglossary\",\"GlossDiv\":{\"title\":\"S\",\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\"," +
                "\"SortAs\":\"SGML\",\"GlossTerm\":\"StandardGeneralizedMarkupLanguage\",\"Acronym\":\"SGML\"," +
                "\"Abbrev\":\"ISO8879:1986\",\"GlossDef\":{\"para\":\"Ameta-markuplanguage," +
                "usedtocreatemarkuplanguagessuchasDocBook.\",\"GlossSeeAlso\":[\"GML\",\"XML\"]}," +
                "\"GlossSee\":\"markup\"}}}}}";
    }
}
