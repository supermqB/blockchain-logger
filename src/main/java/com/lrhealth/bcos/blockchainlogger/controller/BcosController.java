package com.lrhealth.bcos.blockchainlogger.controller;

import cn.hongchain.smledger.sdk.ChainmakerClient;
import com.lrhealth.bcos.blockchainlogger.client.BCOSLoggerService;
import com.lrhealth.bcos.blockchainlogger.contract.entity.LogAsset;
import com.lrhealth.bcos.blockchainlogger.controller.tool.DataTool;

import lombok.extern.slf4j.Slf4j;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.sdk.ResponseInfo;
import org.chainmaker.sdk.User;
import org.chainmaker.sdk.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(description = "日志上链及查验接口")
@Slf4j
@RestController
public class BcosController {
    static Logger logger = LoggerFactory.getLogger(BCOSLoggerService.class);

    public static String CONTRACT_NAME = "logger_contract";

    @Autowired
    BCOSLoggerService client;

    @GetMapping("/create-contract")
    @ApiOperation("创建合约")
    public Object createContract() {

        ResponseInfo responseInfo = null;
        try {
            ContractOuterClass.RuntimeType runtimeType = ContractOuterClass.RuntimeType.forNumber(4);

            byte[] byteCode = FileUtils.getResourceFileBytes("main.wasm");

            // 1. create payload
            byte[] payload = ChainmakerClient.chainClient.createPayloadOfContractCreation(CONTRACT_NAME, "1.0.0", runtimeType, null, byteCode);

            User adminUser1 = new User("wx-org1.chainmaker.org", FileUtils.getResourceFileBytes("crypto-config-test/wx-org1.chainmaker.org/user/admin1/admin1.sign.key"), FileUtils.getResourceFileBytes("crypto-config-test/wx-org1.chainmaker.org/user/admin1/admin1.sign.crt"));

            // 2. create payloads with endorsement
            byte[] payloadWithEndorsement1 = adminUser1.signPayloadOfContractMgmt(payload, ChainmakerClient.chainClient.isEnabledCertHash());

            // 3. merge endorsements using payloadsWithEndorsement
            byte[][] payloadsWithEndorsement = new byte[1][];
            payloadsWithEndorsement[0] = payloadWithEndorsement1;
            byte[] payloadWithEndorsement = ChainmakerClient.chainClient.mergeSignedPayloadsOfContractMgmt(payloadsWithEndorsement);

            // 4. create contract
            responseInfo = ChainmakerClient.chainClient.createContract(payloadWithEndorsement, 10000, 10000);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }

        log.info("================={}",responseInfo);

        return responseInfo.getTxId();
    }

    @ApiOperation(value = "日志上链", notes = "日志上链")
    @RequestMapping(value = { "/addlog" }, method = { RequestMethod.POST })
    public String addlog(@RequestBody LogRequestBean log) {
        LogAsset logAsset = new LogAsset(log.getLogId(), log.getFootprint(), log.getSignature());

        String result = client.addLog(logAsset);

        logger.info("Save the log onto bockchain, id: {}, foortprint: {}, signature: {}", logAsset.getLogId(),
                logAsset.getFootprint(), logAsset.getSignature());
        return result != null ? logAsset.getLogId() : "";
    }

    @ApiOperation(value = "日志查验", notes = "日志查验")
    @RequestMapping(value = { "/verifylog" }, method = { RequestMethod.POST })
    public boolean verifylog(@RequestBody VerifyRequestBean verifyRequest) {
        LogAsset logAsset = client.queryLog(verifyRequest.getLogId());
        logger.info("get the log from bockchain, id: {}, foortprint: {}, signature: {}", logAsset.getLogId(),
                logAsset.getFootprint(), logAsset.getSignature());
        String resignStr = DataTool.signLogConent(verifyRequest.getContent(), logAsset.getSignature());
        return resignStr.equals(logAsset.getFootprint());
    }
}
