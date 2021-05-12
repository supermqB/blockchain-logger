package com.lrhealth.bcos.blockchainlogger.client;

import java.util.List;
import java.util.Map;

import org.fisco.bcos.sdk.config.model.AmopTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix ="bcos")
public class BcosConfig {
    private Map<String, Object> cryptoMaterial;
    public Map<String, List<String> > network;
    public List<AmopTopic> amop;
    public Map<String, Object> account;
    public Map<String, Object> threadPool;

    public Map<String,Object> getCryptoMaterial() {
        return this.cryptoMaterial;
    }

    public void setCryptoMaterial(Map<String,Object> cryptoMaterial) {
        this.cryptoMaterial = cryptoMaterial;
    }

    public Map<String,List<String>> getNetwork() {
        return this.network;
    }

    public void setNetwork(Map<String,List<String>> network) {
        this.network = network;
    }

    public List<AmopTopic> getAmop() {
        return this.amop;
    }

    public void setAmop(List<AmopTopic> amop) {
        this.amop = amop;
    }

    public Map<String,Object> getAccount() {
        return this.account;
    }

    public void setAccount(Map<String,Object> account) {
        this.account = account;
    }

    public Map<String,Object> getThreadPool() {
        return this.threadPool;
    }

    public void setThreadPool(Map<String,Object> threadPool) {
        this.threadPool = threadPool;
    }
}