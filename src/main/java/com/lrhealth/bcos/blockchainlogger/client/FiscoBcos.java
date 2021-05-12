package com.lrhealth.bcos.blockchainlogger.client;

import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.model.ConfigProperty;
import org.fisco.bcos.sdk.model.CryptoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FiscoBcos {

    static Logger logger = LoggerFactory.getLogger(FiscoBcos.class);

    @Autowired
    BcosConfig bcosConfig;

    private BcosSDK bcosSDK;

    public BcosSDK getBcosSDK() {
        return this.bcosSDK;
    }

    @PostConstruct
    public void init() {
        try {
            bcosSDK = new BcosSDK(new ConfigOption(loadProperty(), CryptoType.ECDSA_TYPE));
        } catch (Exception e) {
            logger.error("Errors in bcos initialization.", e);
        }

    }

    public ConfigProperty loadProperty() {
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setCryptoMaterial(bcosConfig.getCryptoMaterial());
        configProperty.setAccount(bcosConfig.getAccount());
        /*
         * damn, org.fisco.bcos.sdk.config.model.ConfigProperty define networks set
         * method as Map, but NetworkConfig class has type conversion to List<String>
         */
        configProperty.setNetwork(new HashMap<String, Object>() {
            {
                put("peers", bcosConfig.getNetwork().get("peers"));
            }
        });
        configProperty.setAmop(bcosConfig.getAmop());
        configProperty.setThreadPool(bcosConfig.getThreadPool());
        return configProperty;
    }
}
