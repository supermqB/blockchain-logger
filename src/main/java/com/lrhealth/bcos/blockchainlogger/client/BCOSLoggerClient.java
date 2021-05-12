package com.lrhealth.bcos.blockchainlogger.client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;


import com.lrhealth.bcos.blockchainlogger.contract.BCOSLogger;
import com.lrhealth.bcos.blockchainlogger.contract.entity.LogAsset;

import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class BCOSLoggerClient {

    static Logger logger = LoggerFactory.getLogger(BCOSLoggerClient.class);

    @Autowired
    private FiscoBcos fiscoBcos;
    private Client client;
    private CryptoKeyPair cryptoKeyPair;

    //@PostConstruct
    public void init() throws Exception {
        logger.info("creating client for group 1......");
        client = fiscoBcos.getBcosSDK().getClient(1);
        cryptoKeyPair = client.getCryptoSuite().createKeyPair();
        client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
        logger.debug("create client for group1, account address is " + cryptoKeyPair.getAddress());
    } 

    public String deployContract() {
        try {
            BCOSLogger loggerContract = BCOSLogger.deploy(client, cryptoKeyPair);
            logger.info(" deploy loggerContract success, contract address is " + loggerContract.getContractAddress());

            recordContractAddr(loggerContract.getContractAddress());
            return loggerContract.getContractAddress();
        } catch (Exception e) {
            logger.error(" deploy Asset contract failed, error message is  " + e.getMessage());
        }
        return "";
    }

    private static String CONTRACT_ADDR_KEY = "logger_contract_address";

    private void recordContractAddr(String address) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.setProperty(CONTRACT_ADDR_KEY, address);
        final Resource contractResource = new ClassPathResource("contract.properties");
        FileOutputStream fileOutputStream = new FileOutputStream(contractResource.getFile());
        prop.store(fileOutputStream, "contract address");
    }

    private static String contractAddr = "";

    public synchronized String getContractAddr() throws Exception {
        if (!"".equals(contractAddr)) {
            return contractAddr;
        }
        // load Asset contact address from contract.properties
        Properties prop = new Properties();
        final Resource contractResource = new ClassPathResource("contract.properties");
        prop.load(contractResource.getInputStream());

        String contractAddress = prop.getProperty(CONTRACT_ADDR_KEY);
        if (contractAddress == null || contractAddress.trim().equals("")) {
            contractAddress = deployContract();
        }
        logger.info(" load Asset address from contract.properties, address is {}", contractAddress);

        contractAddr = contractAddress;
        return contractAddress;
    }

    public int addLog(LogAsset assetLog) {
        int insertedCount = 0;
        try {
            BCOSLogger loggerContract = BCOSLogger.load(getContractAddr(), client, cryptoKeyPair);
            TransactionReceipt receipt = loggerContract.insert(assetLog.getLogId(), assetLog.getFootprint(),
                    assetLog.getSignature());
            List<BCOSLogger.InsertResultEventResponse> response = loggerContract.getInsertResultEvents(receipt);
            if (!response.isEmpty()) {
                insertedCount = response.get(0).count.intValue();
            } else {
                logger.error(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            logger.error("registerLog exception, error message is {}", e.getMessage());
        }
        return insertedCount;
    }

    public LogAsset queryLog(String logId) {
        try {
            BCOSLogger loggerContract = BCOSLogger.load(getContractAddr(), client, cryptoKeyPair);
            Tuple3<List<String>, List<String>, List<String>> queryResult = loggerContract.query(logId);
            return new LogAsset(queryResult.getValue1().get(0), queryResult.getValue2().get(0),
                    queryResult.getValue3().get(0));
        } catch (Exception e) {
            logger.error("registerLog exception, error message is {}", e.getMessage());
            System.out.printf("register log failed, error message is %s\n", e.getMessage());
        }
        return new LogAsset();
    }

    public int removeLog(LogAsset logAsset) {
        try {
            BCOSLogger loggerContract = BCOSLogger.load(getContractAddr(), client, cryptoKeyPair);
            TransactionReceipt receipt = loggerContract.remove(logAsset.getLogId(), logAsset.getFootprint());
            List<BCOSLogger.RemoveResultEventResponse> response = loggerContract.getRemoveResultEvents(receipt);
            if (!response.isEmpty()) {
                return response.get(0).count.intValue();

            }
        } catch (Exception e) {
            logger.error("registerLog exception, error message is {}", e.getMessage());
        }
        return 0;
    }

    /*public static void main(String[] args) throws Exception {

        BCOSLoggerClient client = new BCOSLoggerClient();
        client.init();

        LogAsset logAsset = new LogAsset("log2", "adfs.xxxxxx", "signature");

        // client.insertTable();
        // client.deployContract();
        logger.info("add a log asset, result: {}, id: {}", client.addLog(logAsset), logAsset.getLogId());
        logger.info("add a log asset, result: {}, id: {}", client.addLog(logAsset), logAsset.getLogId());
        logger.info("query log asset with id: {}, its result is: {}", logAsset.getLogId(),
                client.queryLog(logAsset.getLogId()).toString());
        logger.info("%n assets are removed for logid: {}", client.removeLog(logAsset), logAsset.getLogId());
        logger.info("%n assets are removed for logid: {}", client.removeLog(logAsset), logAsset.getLogId());

        System.exit(0);
    }*/
}
