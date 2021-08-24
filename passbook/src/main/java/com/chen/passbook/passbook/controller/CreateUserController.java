package com.chen.passbook.passbook.controller;

import com.chen.passbook.passbook.log.LogConstants;
import com.chen.passbook.passbook.log.LogGenerator;
import com.chen.passbook.passbook.service.IUserService;
import com.chen.passbook.passbook.vo.Response;
import com.chen.passbook.passbook.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 创建用户服务
 *
 * @author Chen on 2021/8/23
 */
@Slf4j
@RestController
@RequestMapping("/passbook")
public class CreateUserController {

    private final IUserService userService;

    private final HttpServletRequest httpServletRequest;

    @Autowired
    public CreateUserController(IUserService userService, HttpServletRequest httpServletRequest) {
        this.userService = userService;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * 创建用户
     *
     * @param user
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/createuser")
    Response createUser(@RequestBody User user) throws Exception {
        LogGenerator.genLog(
                httpServletRequest, -1L, LogConstants.ActionName.CREATE_USER, null);
        return userService.createUser(user);
    }
}
