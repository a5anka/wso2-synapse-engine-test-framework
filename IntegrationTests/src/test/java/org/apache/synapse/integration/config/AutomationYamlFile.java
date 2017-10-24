package org.apache.synapse.integration.config;

import org.apache.synapse.integration.utils.TestUtils;

public class AutomationYamlFile {

    private ServerDetails synapseAgent;
    private ServerDetails synapseServer;
    private ServerDetails backendServer;
    private Axis2ClientConfig axis2ClientConfig;

    /**
     * Getter for synapseServer
     */
    public ServerDetails getSynapseServer() {
        return synapseServer;
    }

    public void setSynapseServer(ServerDetails synapseServer) {
        this.synapseServer = synapseServer;
    }

    /**
     * Getter for backendServer
     */
    public ServerDetails getBackendServer() {
        return backendServer;
    }

    public void setBackendServer(ServerDetails backendServer) {
        this.backendServer = backendServer;
    }

    /**
     * Getter for axis2ClientConfig
     */
    public Axis2ClientConfig getAxis2ClientConfig() {
        return axis2ClientConfig;
    }

    public void setAxis2ClientConfig(
            Axis2ClientConfig axis2ClientConfig) {
        this.axis2ClientConfig = axis2ClientConfig;
    }

    /**
     * Getter for synapseAgent
     */
    public ServerDetails getSynapseAgent() {
        return synapseAgent;
    }

    public void setSynapseAgent(ServerDetails synapseAgent) {
        this.synapseAgent = synapseAgent;
    }

    public static class ServerDetails {
        private String hostname;
        private String port;

        /**
         * Getter for hostname
         */
        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        /**
         * Getter for port
         */
        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getHostAddress() {
            return hostname + ":" + port;
        }
    }

    public static class Axis2ClientConfig {

        private String clientRepo;
        private String axis2Xml;

        public String getClientRepo() {
            return clientRepo;
        }

        public void setClientRepo(String clientRepo) {
            this.clientRepo = TestUtils.getCurrentDir() + clientRepo;
        }

        public String getAxis2Xml() {
            return axis2Xml;
        }

        public void setAxis2Xml(String axis2Xml) {
            this.axis2Xml = TestUtils.getCurrentDir() + axis2Xml;
        }
    }
}
