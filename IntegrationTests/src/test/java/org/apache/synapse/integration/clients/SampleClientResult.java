package org.apache.synapse.integration.clients;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stores the results after executing a client
 */
public class SampleClientResult {

    private AtomicInteger responseCount = new AtomicInteger(0);
    private Exception exception;
    private Properties clientProperties = new Properties();

    public void incrementResponseCount() {
        responseCount.incrementAndGet();
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public boolean responseReceived() {
        return responseCount.get() > 0;
    }

    public Exception getException() {
        return exception;
    }

    public void addProperty(String pName, String pValue){
        clientProperties.setProperty(pName,pValue);
    }

    public String getProperty(String pName){
        return clientProperties.getProperty(pName);
    }
}