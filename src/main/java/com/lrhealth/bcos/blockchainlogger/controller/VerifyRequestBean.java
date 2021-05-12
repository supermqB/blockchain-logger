package com.lrhealth.bcos.blockchainlogger.controller;

public class VerifyRequestBean {
    private String logId;
    private String content;

    public VerifyRequestBean(String logId, String content) {
        this.logId = logId;
        this.content = content;
    }

    public String getLogId() {
        return this.logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}
