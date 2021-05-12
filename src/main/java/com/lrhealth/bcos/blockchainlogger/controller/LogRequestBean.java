package com.lrhealth.bcos.blockchainlogger.controller;

import com.lrhealth.bcos.blockchainlogger.controller.tool.DataTool;

public class LogRequestBean {
    private String userId;
    private String keyURL;
    private String content;
    private String signature = DataTool.getSignature();

    public LogRequestBean() {
    }

    public LogRequestBean(String userId, String keyURL, String content) {
        this.userId = userId;
        this.keyURL = keyURL;
        this.content = content;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKeyURL() {
        return this.keyURL;
    }

    public void setKeyURL(String keyURL) {
        this.keyURL = keyURL;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private String logid;

    public String getLogId() {
        if (logid == null) {
            logid = (new ApiRequestKey(this.getUserId(), this.getKeyURL())).generateLogId();
        }
        return this.logid;
    }

    public String getFootPrint() {
        return DataTool.signLogConent(this.getContent(), this.getSignature());
    }

    public String getSignature() {
        return this.signature;
    }

}
