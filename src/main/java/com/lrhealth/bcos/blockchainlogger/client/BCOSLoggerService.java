package com.lrhealth.bcos.blockchainlogger.client;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lrhealth.bcos.blockchainlogger.contract.entity.LogAsset;
import com.lrhealth.bcos.blockchainlogger.controller.BcosController;

import org.chainmaker.sdk.ResponseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.hongchain.smledger.sdk.ChainmakerClient;
import cn.hutool.core.map.MapUtil;

@Component
public class BCOSLoggerService {

    static Logger logger = LoggerFactory.getLogger(BCOSLoggerService.class);

    /* block chain functions are as below. */
    public String addLog(LogAsset logAsset) {
        String result = invoke(logAsset);
        logger.info("addLog ======================== result " + result);
        return result;
    }

    public LogAsset queryLog(String logId) {
        return query(logId);
    }

    public int updateLog(LogAsset logAsset) {
        int removedCnt = this.removeLog(logAsset);
        this.addLog(logAsset);
        return removedCnt;
    }

    public int removeLog(LogAsset logAsset) {
        try {
           /* BCOSLogger loggerContract = BCOSLogger.load(contractAddr, client, cryptoKeyPair);
            TransactionReceipt receipt = loggerContract.remove(logAsset.getLogId(), logAsset.getFootprint());
            List<BCOSLogger.RemoveResultEventResponse> response = loggerContract.getRemoveResultEvents(receipt);
            if (!response.isEmpty()) {
                return response.get(0).count.intValue();

            }*/
        } catch (Exception e) {
            logger.error("registerLog exception, error message is {}", e.getMessage());
        }
        return 0;
    }

    public String invoke(LogAsset logAsset) {

        Map<String, String> params = MapUtil.newHashMap();

        JSONObject valueJson = new JSONObject();
        valueJson.put("api_request_id", logAsset.getLogId());
        valueJson.put("log_footprint", logAsset.getFootprint());
        valueJson.put("signature", logAsset.getSignature());

        params.put("key",logAsset.getLogId());
        params.put("value",valueJson.toJSONString());

        ResponseInfo responseInfo = ChainmakerClient.invokeContract(BcosController.CONTRACT_NAME, "save", params);

        logger.info("invoke ============================ response:{}", responseInfo);
        if (null == responseInfo) {
            return null;
        }
        return JSON.toJSONString(responseInfo.getTxId());
    }

    public LogAsset query(String logId) {

        Map<String, String> params = MapUtil.newHashMap();
        params.put("key",logId);

        ResponseInfo responseInfo = ChainmakerClient.queryContract(BcosController.CONTRACT_NAME, "find_by_key", params);
        logger.info("query ============================ response:{}", responseInfo.getTxResponse().getContractResult().getResult().toStringUtf8());
        if (null == responseInfo) {
            return null;
        }
        JSONObject result = JSONObject.parseObject(String.valueOf(responseInfo.getTxResponse().getContractResult().getResult().toStringUtf8()));
        JSONObject logAssetJson = result.getJSONObject("value");
        LogAsset logAsset = new LogAsset();
        logAsset.setLogId(logAssetJson.getString("api_request_id"));
        logAsset.setFootprint(logAssetJson.getString("log_footprint"));
        logAsset.setSignature(logAssetJson.getString("signature"));
        return logAsset;
    }

}
