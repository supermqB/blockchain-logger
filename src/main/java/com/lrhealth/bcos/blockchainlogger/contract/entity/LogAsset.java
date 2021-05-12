package com.lrhealth.bcos.blockchainlogger.contract.entity;

import java.util.Objects;

public class LogAsset {

    private String logId;
    private String footprint;
    private String signature;

    public LogAsset() {
    }

    public LogAsset(String logId, String footprint, String signature) {
        this.logId = logId;
        this.footprint = footprint;
        this.signature = signature;
    }

    public LogAsset logId(String logId) {
        setLogId(logId);
        return this;
    }

    public LogAsset footprint(String footprint) {
        setFootprint(footprint);
        return this;
    }

    public LogAsset signature(String signature) {
        setSignature(signature);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof LogAsset)) {
            return false;
        }
        LogAsset assetLog = (LogAsset) o;
        return Objects.equals(logId, assetLog.logId) && Objects.equals(footprint, assetLog.footprint) && Objects.equals(signature, assetLog.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logId, footprint, signature);
    }

    @Override
    public String toString() {
        return "{" +
            " logId='" + getLogId() + "'" +
            ", footprint='" + getFootprint() + "'" +
            ", signature='" + getSignature() + "'" +
            "}";
    }

    public String getLogId() {
        return this.logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getFootprint() {
        return this.footprint;
    }

    public void setFootprint(String footprint) {
        this.footprint = footprint;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
    
}
