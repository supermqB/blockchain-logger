package com.lrhealth.bcos.blockchainlogger.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lrhealth.bcos.blockchainlogger.controller.tool.DataTool;

public class ApiRequestKey {
    private String userId;
    private String keyURL;
    private long ts;
    private int seq;

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

    public long getTs() {
        return this.ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public int getSeq() {
        return this.seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public ApiRequestKey(String userId, String keyURL) {
        this.userId = userId;
        this.keyURL = keyURL;
        this.ts = System.currentTimeMillis();
        this.seq = DataTool.getSeq(this.ts);
    }

    public String generateLogId() {
        String logId = null;
        try {
            logId = DataTool.md5((new ObjectMapper()).writeValueAsString(this));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return logId;
    }
}