package com.chen.passbook.passbook.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 日志对象
 *
 * @author Chen on 2021/8/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogObject {

    /**
     * 日志动作类型，对应LogConstants中的内容
     */
    private String action;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 当前时间戳
     */
    private Long timestamp;

    /**
     * 客户端 ip 地址
     */
    private String remoteIp;

    /**
     * 日志信息
     */
    private Object info = null;
}
