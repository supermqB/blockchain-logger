package com.lrhealth.bcos.blockchainlogger.controller;

import com.lrhealth.bcos.blockchainlogger.client.BCOSLoggerService;
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
    static Logger logger = LoggerFactory.getLogger(BCOSLoggerService.class);

    @Autowired
    BCOSLoggerService client;

    @ApiOperation(value = "日志上链", notes = "日志上链")
    @RequestMapping(value = { "/addlog" }, method = { RequestMethod.POST })
    public String addlog(@RequestBody LogRequestBean log) {
        LogAsset logAsset = new LogAsset(log.getLogId(), log.getFootprint(), log.getSignature());

        int insertedCount = client.addLog(logAsset);

        logger.info("Save the log onto bockchain, id: {}, foortprint: {}, signature: {}", logAsset.getLogId(),
                logAsset.getFootprint(), logAsset.getSignature());
        if (insertedCount > 0) {
            return logAsset.getLogId();
        } else {
            return "";
        }
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
