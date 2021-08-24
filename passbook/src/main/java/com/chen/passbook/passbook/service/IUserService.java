package com.chen.passbook.passbook.service;

import com.chen.passbook.passbook.vo.Response;
import com.chen.passbook.passbook.vo.User;

/**
 * 用户服务：创建 User 服务
 *
 * @author Chen on 2021/8/21
 */
public interface IUserService {

    /**
     * 创建用户
     *
     * @param user
     * @return
     * @throws Exception
     */
    Response createUser(User user) throws Exception;
}
