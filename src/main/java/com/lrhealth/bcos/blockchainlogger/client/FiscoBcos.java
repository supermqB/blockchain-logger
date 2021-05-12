package com.lrhealth.bcos.blockchainlogger.client;

import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.config.ConfigOption;
import org.fisco.bcos.sdk.config.exceptions.ConfigException;
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

    BcosSDK bcosSDK;

    public BcosSDK getBcosSDK() {
        if(bcosSDK == null) {
            this.init();
        }
        return this.bcosSDK;
    }

    public void setBcosSDK(BcosSDK bcosSDK) {
        this.bcosSDK = bcosSDK;
    }

    @PostConstruct
    public void init() {
        ConfigProperty configProperty = loadProperty();
        ConfigOption configOption;
        try {
            configOption = new ConfigOption(configProperty, CryptoType.ECDSA_TYPE);
        } catch (ConfigException e) {
            logger.error("init error:" + e.toString());
            return;
        }

        try {
            bcosSDK = new BcosSDK(configOption);
            /* @SuppressWarnings("resource")
            ApplicationContext context = new ClassPathXmlApplicationContext("classpath:fisco_config.xml");
            bcosSDK = context.getBean(BcosSDK.class); */
            logger.info("sdk is created!!!!");
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            logger.info("get into final!!!!");
        }
    }

    public ConfigProperty loadProperty() {
        ConfigProperty configProperty = new ConfigProperty();
        configProperty.setCryptoMaterial(bcosConfig.getCryptoMaterial());
        configProperty.setAccount(bcosConfig.getAccount());
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
