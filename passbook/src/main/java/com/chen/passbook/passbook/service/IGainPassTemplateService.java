package com.chen.passbook.passbook.service;

import com.chen.passbook.passbook.vo.GainPassTemplateRequest;
import com.chen.passbook.passbook.vo.Response;

/**
 * 用户领取优惠券功能实现
 *
 * @author Chen on 2021/8/22
 */
public interface IGainPassTemplateService {

    /**
     * 用户领取优惠券
     *
     * @param request
     * @return
     * @throws Exception
     */
    Response gainPassTemplate(GainPassTemplateRequest request) throws Exception;
}
