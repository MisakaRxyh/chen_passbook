package com.chen.passbook.passbook.service;

import com.chen.passbook.passbook.vo.Response;

/**
 * 获取库存信息：只返回用户没有领取的，即优惠券库存功能实现接口定义
 *
 * @author Chen on 2021/8/22
 */
public interface IInventoryService {

    /**
     * 获取库存信息
     *
     * @param userId
     * @return
     * @throws Exception
     */
    Response getInventoryInfo(Long userId) throws Exception;
}
