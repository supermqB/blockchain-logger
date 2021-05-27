package com.lrhealth.bcos.blockchainlogger.client;

import java.util.List;

import com.cnhealth.devcenter.fiscoclient.AbstractContractService;
import com.lrhealth.bcos.blockchainlogger.contract.BCOSLogger;
import com.lrhealth.bcos.blockchainlogger.contract.entity.LogAsset;

import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BCOSLoggerService extends AbstractContractService {

    static Logger logger = LoggerFactory.getLogger(BCOSLoggerService.class);

    /* 使用链上已经部署合约，需配置。 */
    @Value("${bcos.contract.logger.address}")
    private String contractAddr;

    public String getContractAddress() {
        return this.contractAddr;
    };

    public void deployContract() throws ContractException {
        contractAddr = BCOSLogger.deploy(client, cryptoKeyPair).getContractAddress();
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
                logger.error(
                        "insert event is not found, maybe transaction not exec, please check the contract is properly deployed, or an incorrect contract is used.");
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
