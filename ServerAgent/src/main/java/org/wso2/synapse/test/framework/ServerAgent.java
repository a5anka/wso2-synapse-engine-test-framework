/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.synapse.test.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.msf4j.HttpStreamHandler;
import org.wso2.msf4j.HttpStreamer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * This is a Agent to run in synapse server machine
 *
 * @since 1.0-SNAPSHOT
 */
@Path("/synapseAgent")
public class ServerAgent {

    private static final String DEFAULT_SYNAPSE_HOME_LOCATION = ".";
    public static final String INTEGRATION_SYNAPSE_XML = "integration-synapse.xml";

    private ServerLogReader inputStreamHandler;
    private ServerLogReader errorStreamHandler;

    private static final Log log = LogFactory.getLog(ServerAgent.class);

    private Process process;

    @GET
    @Path("/start")
    public void startServer() {

        try {
            String synapseHomeLocation = getSynapseHome();

            File synapseHome = Paths.get(synapseHomeLocation).toFile();

            String[] cmdArray;
            // For Windows
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                cmdArray = new String[]{ "cmd.exe", "/c", synapseHomeLocation + File.separator  + "bin" + File.separator +
                        "synapse.bat", "-synapseConfig", synapseHomeLocation + File.separator + "repository"
                        + File.separator + "conf" + File.separator + INTEGRATION_SYNAPSE_XML};
            } else {
                // For Unix
                cmdArray = new String[]{ "sh", synapseHomeLocation + File.separator + "bin" + File.separator +
                        "synapse.sh", "-synapseConfig", synapseHomeLocation + File.separator + "repository"
                        + File.separator + "conf" + File.separator + INTEGRATION_SYNAPSE_XML};
            }

            process = Runtime.getRuntime().exec(cmdArray, null, synapseHome);

            errorStreamHandler = new ServerLogReader("errorStream", process.getErrorStream());
            inputStreamHandler = new ServerLogReader("inputStream", process.getInputStream());

            // start the stream readers
            inputStreamHandler.start();
            errorStreamHandler.start();

        } catch (Exception ex) {
            log.error("Error while starting synapse server", ex);
        }

    }

    private String getSynapseHome() {
        return System.getProperty("synapse.home", DEFAULT_SYNAPSE_HOME_LOCATION);
    }

    @GET
    @Path("/stop")
    public void stopServer() {
        if (process != null) {
            try {
                String synapseKillCommand = getSynapseHome() + File.separator + "bin" + File.separator + "synapse-stop.sh";
                Runtime.getRuntime().exec(synapseKillCommand);
            } catch (IOException e) {
                log.error("Error while stopping synapse server", e);
            }
            inputStreamHandler.stop();
            errorStreamHandler.stop();
            process = null;
        }
    }

    /**
     * Upload a file with streaming.
     *
     * @param httpStreamer Handle for setting the {@link HttpStreamHandler}callback for streaming.
     * @throws IOException
     */
    @POST
    @Path("/upload-config")
    public void postFile(@Context HttpStreamer httpStreamer) throws IOException {
        httpStreamer.callback(new HttpStreamHandlerImpl(
                getSynapseHome() + File.separator + "repository" + File.separator + "conf" + File.separator
                        + INTEGRATION_SYNAPSE_XML));
    }

    private static class HttpStreamHandlerImpl implements HttpStreamHandler {
        private static final String SYNAPSE_SAMPLE_DIR = DEFAULT_SYNAPSE_HOME_LOCATION + File.separator + "repository"
                + File.separator + "conf";
        private FileChannel fileChannel = null;
        private org.wso2.msf4j.Response response;

        HttpStreamHandlerImpl(String fileName) throws FileNotFoundException {
            File file = Paths.get(fileName).toFile();
            if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
                fileChannel = new FileOutputStream(file).getChannel();
            }
        }

        @Override
        public void init(org.wso2.msf4j.Response response) {
            this.response = response;
        }

        @Override
        public void end() throws Exception {
            fileChannel.close();
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            response.send();
        }

        @Override
        public void chunk(ByteBuffer content) throws Exception {
            if (fileChannel == null) {
                throw new IOException("Unable to write file");
            }
            fileChannel.write(content);
        }

        @Override
        public void error(Throwable cause) {
            try {
                if (fileChannel != null) {
                    fileChannel.close();
                }
            } catch (IOException e) {
                // Log if unable to close the output stream
                log.error("Unable to close file output stream", e);
            }
        }
    }
}
