package com.lrhealth.bcos.blockchainlogger.client;

import java.util.List;

import javax.annotation.PostConstruct;

import com.lrhealth.bcos.blockchainlogger.contract.BCOSLogger;
import com.lrhealth.bcos.blockchainlogger.contract.entity.LogAsset;

import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BCOSLoggerClient {

    static Logger logger = LoggerFactory.getLogger(BCOSLoggerClient.class);

    @Autowired
    private FiscoBcos fiscoBcos;

    private Client client;
    private CryptoKeyPair cryptoKeyPair;
    @Value("${bcos.contract.logger.address}")
    private String contractAddr;

    @PostConstruct
    public void init() throws Exception {
        client = fiscoBcos.getBcosSDK().getClient(1);
        cryptoKeyPair = client.getCryptoSuite().getCryptoKeyPair();

        logger.info("create client for group1, with account address: {}", cryptoKeyPair.getAddress());

        if (contractAddr == null || "".equals(contractAddr)) {
            deployContract();
        }
    }

    public void deployContract() {
        try {
            BCOSLogger loggerContract = BCOSLogger.deploy(client, cryptoKeyPair);
            logger.info("deploy loggerContract success, contract address is " + loggerContract.getContractAddress());
            contractAddr = loggerContract.getContractAddress();
        } catch (Exception e) {
            logger.error("deploy Asset contract failed, error message is  " + e.getMessage());
        }
    }

    /* block chain functions are as below. */
    public int addLog(LogAsset logAsset) {
        int insertedCount = 0;
        try {
            BCOSLogger loggerContract = BCOSLogger.load(contractAddr, client, cryptoKeyPair);
            TransactionReceipt receipt = loggerContract.insert(logAsset.getLogId(), logAsset.getFootprint(),
                    logAsset.getSignature());
            List<BCOSLogger.InsertResultEventResponse> response = loggerContract.getInsertResultEvents(receipt);
            if (!response.isEmpty()) {
                insertedCount = response.get(0).count.intValue();
            } else {
                logger.error("insert event is not found, maybe transaction not exec, please check the contract is properly deployed, or an incorrect contract is used.");
            }
        } catch (Exception e) {
            logger.error("registerLog exception, error message is {}", e.getMessage());
        }
        return insertedCount;
    }

    public LogAsset queryLog(String logId) {
        try {
            BCOSLogger loggerContract = BCOSLogger.load(contractAddr, client, cryptoKeyPair);
            Tuple3<List<String>, List<String>, List<String>> queryResult = loggerContract.query(logId);
            return new LogAsset(queryResult.getValue1().get(0), queryResult.getValue2().get(0),
                    queryResult.getValue3().get(0));
        } catch (Exception e) {
            logger.error("registerLog exception, error message is {}", e.getMessage());
            System.out.printf("register log failed, error message is %s\n", e.getMessage());
        }
        return new LogAsset();
    }

    public int updateLog(LogAsset logAsset) {
        int removedCnt = this.removeLog(logAsset);
        this.addLog(logAsset);
        return removedCnt;
    }

    public int removeLog(LogAsset logAsset) {
        try {
            BCOSLogger loggerContract = BCOSLogger.load(contractAddr, client, cryptoKeyPair);
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
}
