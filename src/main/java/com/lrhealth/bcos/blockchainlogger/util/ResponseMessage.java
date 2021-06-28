package com.lrhealth.bcos.blockchainlogger.util;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author YangChao
 * @create 2020-11-22 19:16
 **/
@ApiModel(description = "响应结果")
@Data
public class ResponseMessage<T> implements Serializable {

    @ApiModelProperty("状态码")
    protected int status;

    @ApiModelProperty("错误消息")
    protected String message;

    @ApiModelProperty("成功时响应数据")
    protected T result;

    public T getResult() {
        return result;
    }

    @ApiModelProperty(value = "业务自定义状态码")
    protected String code;
    public String getCode() {
        return code;
    }

    @ApiModelProperty(value = "时间戳", required = true, dataType = "Long")
    private Long timestamp;

}