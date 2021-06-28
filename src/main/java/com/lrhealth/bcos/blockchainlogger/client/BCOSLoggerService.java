package com.lrhealth.bcos.blockchainlogger.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lrhealth.bcos.blockchainlogger.contract.entity.LogAsset;
import com.lrhealth.bcos.blockchainlogger.util.ResponseMessage;
import com.lrhealth.bcos.blockchainlogger.util.RestTemplateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollectionUtil;

@Component
public class BCOSLoggerService {
    @Value("${hl.manager.address.ip}")
    private String HL_IP;
    @Value("${hl.manager.address.port}")
    private String HL_REST_PORT;

    private String CHANNAL_NAME="test-channel";

    private Logger logger = LoggerFactory.getLogger(BCOSLoggerService.class);

    /* block chain functions are as below. */
    public int addLog(LogAsset logAsset) {
        String transactionAddr = invoke(logAsset);
        logger.info("addLog ======================== transactionAddr " + transactionAddr);
        return transactionAddr == null ? 0 : 1;
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
        JSONObject object = new JSONObject();
        object.put("api_request_id", logAsset.getLogId());
        object.put("log_footprint", logAsset.getFootprint());
        object.put("signature", logAsset.getSignature());

        List<String> arguments = CollectionUtil.newArrayList();
        arguments.add(object.toJSONString());

        List<String> peerNames = Collections.singletonList("peer0.Org1");

        JSONObject jsonObject = new JSONObject();
        // 通道名称
        jsonObject.put("channel_name", CHANNAL_NAME);
        // 函数名称
        jsonObject.put("func_name", "saveBFInfo");
        // 参数数组
        jsonObject.put("args", arguments.toArray(new Object[0]));
        // 节点名称
        jsonObject.put("peers", peerNames.toArray(new String[0]));
        // 链码名称
        jsonObject.put("chaincode_name", "bfcc");
        logger.info("invoke ============================ request:{}", JSON.toJSONString(jsonObject));
        String contractRestAddr = String.format("http://%s:%s/manage/invoke",HL_IP, HL_REST_PORT);
        logger.info("save API log contract rest addres is: {}", contractRestAddr);
        ResponseMessage response = RestTemplateUtil.post(contractRestAddr, jsonObject, ResponseMessage.class);
        logger.info("invoke ============================ response:{}", JSONObject.toJSONString(response));
        if (null == response) {
            return null;
        }
        if (!"0".equals(response.getCode())) {
            return null;
        }
        return JSON.toJSONString(response.getResult());
    }

    public LogAsset query(String logId) {
        List<String> arguments = new ArrayList<>();
        arguments.add(logId);

        JSONObject jsonObject = new JSONObject();
        // 通道名称
        jsonObject.put("channel_name", CHANNAL_NAME);
        // 函数名称
        jsonObject.put("func_name", "BFInfoById");
        // 参数数组
        jsonObject.put("args", arguments.toArray(new String[0]));
        // 链码名称
        jsonObject.put("chaincode_name", "bfcc");
        // 节点名称
        jsonObject.put("peer_name", "peer0.Org1");
        logger.info("query ============================ request:{}", JSON.toJSONString(jsonObject));
        ResponseMessage response = RestTemplateUtil.post("http://" + HL_IP + ":"+HL_REST_PORT+"/manage/query", jsonObject, ResponseMessage.class);
        logger.info("query ============================ response:{}", JSONObject.toJSONString(response));
        if (null == response) {
            return null;
        }
        if (!"0".equals(response.getCode())) {
            return null;
        }
        JSONObject result = JSONObject.parseObject(String.valueOf(response.getResult()));
        JSONArray jsonArray = result.getJSONArray("bfInfo");
        if (null == jsonArray) {
            return null;
        }
        JSONObject object = jsonArray.getJSONObject(0);
        LogAsset logAsset = new LogAsset();
        logAsset.setLogId(object.getString("api_request_id"));
        logAsset.setFootprint(object.getString("log_footprint"));
        logAsset.setSignature(object.getString("signature"));
        return logAsset;
    }

}
