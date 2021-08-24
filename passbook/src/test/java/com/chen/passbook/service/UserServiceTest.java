package com.chen.passbook.service;

import com.alibaba.fastjson.JSON;
import com.chen.passbook.passbook.service.IUserService;
import com.chen.passbook.passbook.vo.Response;
import com.chen.passbook.passbook.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 用户服务测试
 *
 * @author Chen on 2021/8/23
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {
    @Autowired
    private IUserService userService;

    @Test
    public void testCreateUser() throws Exception{
        User user = new User();
        user.setBaseInfo(
                new User.BaseInfo("chen", 24, "m")
        );
        user.setOtherInfo(
                new User.OtherInfo("13988765622","安徽省合肥市")
        );
        Response res = userService.createUser(user);
        System.out.println(JSON.toJSONString(res));
    }
}
