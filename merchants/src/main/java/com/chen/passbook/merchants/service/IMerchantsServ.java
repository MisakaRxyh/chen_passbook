package com.chen.passbook.merchants.service;

import com.chen.passbook.merchants.vo.CreateMerchantsRequest;
import com.chen.passbook.merchants.vo.PassTemplate;
import com.chen.passbook.merchants.vo.Response;

/**
 * 对商户服务的接口定义
 */
public interface IMerchantsServ {
    /**
     * 创建商户服务
     * @param request
     * @return
     */
    Response createMerchants(CreateMerchantsRequest request);

    /**
     * 根据 id 构造商户信息
     * @param id
     * @return
     */
    Response buildMerchantsInfoById(Integer id);

    /**
     * 投放优惠券
     * @param template
     * @return
     */
    Response dropPassTemplate(PassTemplate template);
}
