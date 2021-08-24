package com.chen.passbook.passbook.service;

import com.chen.passbook.passbook.vo.Pass;
import com.chen.passbook.passbook.vo.Response;

import java.lang.annotation.Repeatable;

/**
 * 获取用户个人优惠券信息
 *
 * @author Chen on 2021/8/22
 */
public interface IUserPassService {

    /**
     * 获取用户个人优惠券信息，即我的优惠券功能实现
     *
     * @param userId
     * @return
     * @throws Exception
     */
    Response getUserPassInfo(Long userId) throws Exception;

    /**
     * 获取用户已经消费了的优惠券，即已使用的优惠券功能实现
     *
     * @param userId
     * @return
     * @throws Exception
     */
    Response getUserUsedPassInfo(Long userId) throws Exception;

    /**
     * 获取用户所有的优惠券
     *
     * @param userId
     * @return
     * @throws Exception
     */
    Response getUserAllPassInfo(Long userId) throws Exception;

    /**
     * 用户使用优惠券
     * @param pass
     * @return
     */
    Response userUsePass(Pass pass);
}
