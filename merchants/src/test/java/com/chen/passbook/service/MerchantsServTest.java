package com.chen.passbook.service;

import com.alibaba.fastjson.JSON;
import com.chen.passbook.merchants.service.IMerchantsServ;
import com.chen.passbook.merchants.vo.CreateMerchantsRequest;
import com.chen.passbook.merchants.vo.PassTemplate;
import org.apache.commons.lang.time.DateUtils;
import org.assertj.core.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 商户服务测试类
 *
 * @author Chen on 2021/8/20
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MerchantsServTest {
    @Autowired
    private IMerchantsServ merchantsServ;

    @Test
    @Transactional
    public void testCreateMerchantsServ(){
        CreateMerchantsRequest request = new CreateMerchantsRequest();
        request.setName("安建大");
        request.setLogoUrl("www.ahjzu.com");
        request.setBusinessLicenseUrl("www.chen.com");
        request.setPhone("1234325123");
        request.setAddress("安徽省");

        System.out.println(JSON.toJSONString(merchantsServ.createMerchants(request)));
    }

    @Test
    @Transactional
    public void testBuildMerchantsInfoById(){
        System.out.println(JSON.toJSONString(merchantsServ.buildMerchantsInfoById(17)));
    }

    @Test
    @Transactional
    public void testDropPassTemplate(){
        PassTemplate passTemplate = new PassTemplate();
        passTemplate.setId(17);
        passTemplate.setTitle("安建大-1");
        passTemplate.setSummary("简介：安建大");
        passTemplate.setDesc("详情：安建大");
        passTemplate.setLimit(10000L);
        passTemplate.setHasToken(false);
        passTemplate.setBackground(2);
        passTemplate.setStart(DateUtils.addDays(new Date(), -10));
        passTemplate.setEnd(DateUtils.addDays(new Date(), 10));

        System.out.println(JSON.toJSONString(
                merchantsServ.dropPassTemplate(passTemplate)
        ));
    }
}
