package com.chen.passbook.merchants.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用的响应对象
 *
 * @author Chen on 2021/8/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    /**
     * 错误码，正确返回0
     */
    private Integer errorCode = 0;

    /**
     * 错误信息，正确返回空字符串
     */
    private String errorMsg = "";

    /**
     * 返回值对象
     */
    private Object data;

    /**
     * 正确的响应构造函数
     * @param data
     */
    public Response(Object data){
        this.data = data;
    }
}
