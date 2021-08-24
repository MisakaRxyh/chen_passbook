package com.chen.passbook.merchants.controller;

import com.alibaba.fastjson.JSON;
import com.chen.passbook.merchants.service.IMerchantsServ;
import com.chen.passbook.merchants.vo.CreateMerchantsRequest;
import com.chen.passbook.merchants.vo.PassTemplate;
import com.chen.passbook.merchants.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商户服务 Controller
 *
 * @author Chen on 2021/8/21
 */
@Slf4j
@RestController
@RequestMapping("/merchants")
public class MerchantsController {

    /**
     * 商户服务接口
     */
    private final IMerchantsServ merchantsServ;

    @Autowired
    public MerchantsController(IMerchantsServ merchantsServ) {
        this.merchantsServ = merchantsServ;
    }

    @ResponseBody
    @PostMapping("/create")
    public Response createMerchants(@RequestBody CreateMerchantsRequest request) {
        log.info("CreateMerchants:{}", JSON.toJSONString(request));
        return merchantsServ.createMerchants(request);
    }

    @ResponseBody
    @GetMapping("/{id}")
    public Response buildMerchantsInfo(@PathVariable Integer id) {
        log.info("BuildMerchantsInfo:{}", id);
        return merchantsServ.buildMerchantsInfoById(id);
    }


    @ResponseBody
    @PostMapping("/drop")
    public Response dropPassTemplate(@RequestBody PassTemplate passTemplate) {
        log.info("DropPassTemplate:{}", passTemplate);
        return merchantsServ.dropPassTemplate(passTemplate);
    }
}
