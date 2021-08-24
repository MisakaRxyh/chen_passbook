package com.chen.passbook.passbook.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * 日志生成器
 *
 * @author Chen on 2021/8/21
 */
@Slf4j
public class LogGenerator {
    /**
     * 生成 log
     * @param request 通过request获取用户ip
     * @param userId
     * @param action
     * @param info
     */
    public static void genLog(HttpServletRequest request, Long userId, String action, Object info) {
        log.info(JSON.toJSONString(
                new LogObject(action,userId,System.currentTimeMillis(),request.getRemoteAddr(),info)
        ));
    }
}
