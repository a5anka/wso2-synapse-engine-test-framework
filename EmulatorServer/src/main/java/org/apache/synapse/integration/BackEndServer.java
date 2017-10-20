package org.apache.synapse.integration;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.wso2.carbon.protocol.emulator.dsl.Emulator;
import org.wso2.carbon.protocol.emulator.http.server.contexts.HttpServerOperationBuilderContext;

import java.io.File;
import java.io.IOException;

import static org.wso2.carbon.protocol.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.carbon.protocol.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.carbon.protocol.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

public class BackEndServer {
    
    private static String hostIp = "10.100.8.3";
    public static void main(String[] args) throws IOException {
        startHttpEmulatorCopyHeaders();
        startHttpEmulatorMalformedPayload();
        startHttpEmulatorWritingConnectionDrop();
        startHttpEmulatorReadingConnectionDrop();
        startHttpEmulatorChunkingDisabled();
        startHttpEmulatorSlowWriter();
        startHttpEmulatorHttpVersion10();
        startHttpEmulatorKeepAlive();
        startHttpEmulatorSlowResponse();
        startHttpEmulatorLargePayload();
        startHttpEmulatorWithReadingDelay();
        startHttpEmulatorRandomDrop();
        startHttpEmulator();
        startHttpEmulatorMissingHeader();
        startHttpInvalidSpec();
        startHttpEmulatorSlowWritingLargePayload();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorLargePayload() throws IOException {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6066).context("/large").withEnableWireLog()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/response")
                )
                .then(
                        response().withBody(new File("1MB.txt")).withStatusCode(HttpResponseStatus.OK)
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorSlowResponse() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6065).context("/slow").withLogicDelay(3000)
                                .withEnableWireLog()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/response")
                )
                .then(
                        response().withBody("Slowly responding backend").withStatusCode(HttpResponseStatus.OK)
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorWithReadingDelay() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6067).context("/reading").withReadingDelay(3000)
                                .withEnableWireLog()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/delay")
                )
                .then(
                        response().withBody("Slowly reading backend").withStatusCode(HttpResponseStatus.OK)
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorKeepAlive() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6068).context("/keep").withDisableKeepAlive()
                                .withEnableWireLog()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/alive")
                )
                .then(
                        response().withBody("Keep alive").withStatusCode(HttpResponseStatus.OK)
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorHttpVersion10() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6069).context("/version").withEnableWireLog()
                                .withHttpVersion(HttpVersion.HTTP_1_0)
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/10")
                )
                .then(
                        response().withBody("HTTP version 1.0 only\n").withStatusCode(HttpResponseStatus.OK)
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorSlowWriter() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6070).context("/slow").withEnableWireLog()
                                .withWritingDelay(3000)
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/writer")
                )
                .then(
                        response().withBody("Slowly writing backend").withStatusCode(HttpResponseStatus.OK)
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorChunkingDisabled() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6071).context("/chunking").withEnableWireLog()
                                .withDisabledChunking()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/disabled")
                )
                .then(
                        response().withBody("Chunking disabled").withStatusCode(HttpResponseStatus.OK)
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorReadingConnectionDrop() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6072).context("/reading").withEnableWireLog()
                                .withEnableReadingConnectionDrop()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/drop")
                )
                .then(
                        response().withBody("Reading connection dropped").withStatusCode(HttpResponseStatus.OK)
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorWritingConnectionDrop() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6073).context("/writing").withEnableWireLog()
                                .withEnableWritingConnectionDrop()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/drop")
                )
                .then(
                        response().withBody("Writing connection dropped").withStatusCode(HttpResponseStatus.OK)
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorMalformedPayload() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6074).context("/malformed").withEnableWireLog()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/payload")
                )
                .then(
                        response().withBody("Malformed XML payload").withStatusCode(HttpResponseStatus.OK).withBody(
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                        "<note>\n" +
                                        "  <to>Tove<to>\n" +
                                        "  <from>Jani</from>\n" +
                                        "  <heading>Reminder</heading>\n" +
                                        "  <body>Don't forget me this weekend!</body>\n" +
                                        "</note>")
                                .withHeader("Content-Type", "application/xml").withHeader("wso2", "123")
                )
                .operation()
                .start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorCopyHeaders() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6075).context("/copy").withEnableWireLog()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/headers")
                )
                .then(
                        response().withBody("Malformed JSON payload").withStatusCode(HttpResponseStatus.OK).withBody(
                                "{\"glossary\":{\"title\":\"exampleglossary\",\"GlossDiv\":{\"title\":\"S\"," +
                                        "\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\"," +
                                        "\"GlossTerm\":\"StandardGeneralizedMarkupLanguage\",\"Acronym\":\"SGML\"," +
                                        "\"Abbrev\":\"ISO8879:1986\",\"GlossDef\":{\"para\":\"Ameta-markuplanguage," +
                                        "usedtocreatemarkuplanguagessuchasDocBook.\",\"GlossSeeAlso\":[\"GML\"," +
                                        "\"XML\"]},\"GlossSee\":\"markup\"}}}}}")
                                .withHeader("Content-Type", "application/json").withCopyHeader("wso2")
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorRandomDrop() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6076).context("/random").withEnableWireLog()
                                .randomConnectionClose(true).withDisableKeepAlive()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/drop")
                )
                .then(
                        response().withBody("Malformed JSON payload").withStatusCode(HttpResponseStatus.OK).withBody(
                                "{\"glossary\":{\"title\":\"exampleglossary\",\"GlossDiv\":{\"title\":\"S\"," +
                                        "\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\"," +
                                        "\"GlossTerm\":\"StandardGeneralizedMarkupLanguage\",\"Acronym\":\"SGML\"," +
                                        "\"Abbrev\":\"ISO8879:1986\",\"GlossDef\":{\"para\":\"Ameta-markuplanguage," +
                                        "usedtocreatemarkuplanguagessuchasDocBook.\",\"GlossSeeAlso\":[\"GML\"," +
                                        "\"XML\"]},\"GlossSee\":\"markup\"}}}}}")
                                .withHeader("Content-Type", "application/json")
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6077).context("/normal").withEnableWireLog()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/server")
                )
                .then(
                        response().withBody("Malformed JSON payload").withStatusCode(HttpResponseStatus.OK).withBody(
                                "{\"glossary\":{\"title\":\"exampleglossary\",\"GlossDiv\":{\"title\":\"S\"," +
                                        "\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\"," +
                                        "\"GlossTerm\":\"StandardGeneralizedMarkupLanguage\",\"Acronym\":\"SGML\"," +
                                        "\"Abbrev\":\"ISO8879:1986\",\"GlossDef\":{\"para\":\"Ameta-markuplanguage," +
                                        "usedtocreatemarkuplanguagessuchasDocBook.\",\"GlossSeeAlso\":[\"GML\"," +
                                        "\"XML\"]},\"GlossSee\":\"markup\"}}}}}")
                                .withHeader("Content-Type", "application/json")
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorMissingHeader() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6078).context("/missing").withEnableWireLog()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/header")
                )
                .then(
                        response().withStatusCode(HttpResponseStatus.OK).withBody(
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                        "<note>\n" +
                                        "  <to>Tove</to>\n" +
                                        "  <from>Jani</from>\n" +
                                        "  <heading>Reminder</heading>\n" +
                                        "  <body>Don't forget me this weekend!</body>\n" +
                                        "</note>")
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpInvalidSpec() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6079).context("/invalid").withEnableWireLog()
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/spec")
                )
                .then(
                        response().withStatusCode(HttpResponseStatus.OK).withBody(
                                "This is a plain body while the content type header says application/xml")
                                .withHeader(HttpHeaders.Names.CONTENT_TYPE, "application/xml")
                )

                .operation().start();
    }

    private static HttpServerOperationBuilderContext startHttpEmulatorSlowWritingLargePayload() {
        return Emulator.getHttpEmulator().server()
                .given(
                        configure().host(hostIp).port(6080).context("/slow").withEnableWireLog()
                                .withWritingDelay(3000)
                )

                .when(
                        request().withMethod(HttpMethod.POST).withPath("/large")
                )
                .then(
                        response().withStatusCode(HttpResponseStatus.OK).withBody(new File("1MB.txt"))
                )
                .operation().start();
    }
}

