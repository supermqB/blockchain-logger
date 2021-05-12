package com.lrhealth.bcos.blockchainlogger.controller;

import com.lrhealth.bcos.blockchainlogger.client.BCOSLoggerClient;
import com.lrhealth.bcos.blockchainlogger.contract.entity.LogAsset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BcosController {
    static Logger logger = LoggerFactory.getLogger(BCOSLoggerClient.class);

    @Autowired
    BCOSLoggerClient client;

    @RequestMapping("/")
    public String index() {
        try {
            client.init();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        LogAsset logAsset = new LogAsset("log2", "adfs.xxxxxx", "signature");

        // client.insertTable();
        // client.deployContract();
        logger.info("add a log asset, result: {}, id: {}", client.addLog(logAsset), logAsset.getLogId());
        logger.info("add a log asset, result: {}, id: {}", client.addLog(logAsset), logAsset.getLogId());
        logger.info("query log asset with id: {}, its result is: {}", logAsset.getLogId(),
                client.queryLog(logAsset.getLogId()).toString());
        logger.info("{} assets are removed for logid: {}", client.removeLog(logAsset), logAsset.getLogId());
        logger.info("{} assets are removed for logid: {}", client.removeLog(logAsset), logAsset.getLogId());
        return "yes, I'm done!";
    }
}
