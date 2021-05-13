package com.lrhealth.bcos.blockchainlogger.controller;

import com.lrhealth.bcos.blockchainlogger.client.BCOSLoggerClient;
import com.lrhealth.bcos.blockchainlogger.contract.entity.LogAsset;
import com.lrhealth.bcos.blockchainlogger.controller.tool.DataTool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(description = "日志上链及查验接口")
@RestController
public class BcosController {
    static Logger logger = LoggerFactory.getLogger(BCOSLoggerClient.class);

    @Autowired
    BCOSLoggerClient client;

    @ApiOperation(value="日志上链", notes="日志上链")
    @RequestMapping(value = { "/addlog" }, method = { RequestMethod.POST })
    public String addlog(@RequestBody LogRequestBean log) {
        LogAsset logAsset = new LogAsset(log.getLogId(), log.getFootPrint(), log.getSignature());

        int insertedCount = client.addLog(logAsset);

        logger.info("Save the log onto bockchain, id: {}, foortprint: {}, signature: {}", log.getLogId(),
                log.getFootPrint(), log.getSignature());
        if (insertedCount > 0) {
            return log.getLogId();
        } else {
            return "";
        }
    }

    @ApiOperation(value="日志查验", notes="日志查验")
    @RequestMapping(value = { "/verifylog" }, method = { RequestMethod.POST })
    public boolean verifylog(@RequestBody VerifyRequestBean verifyRequest) {
        LogAsset logAsset = client.queryLog(verifyRequest.getLogId());
        String resignStr = DataTool.signLogConent(verifyRequest.getContent(), logAsset.getSignature());
        return resignStr.equals(logAsset.getFootprint());
    }
}
