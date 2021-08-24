package com.chen.passbook.merchants.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建商户的响应对象
 *
 * @author Chen on 2021/8/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMerchantsResponse {
    /**
     * 商户 id：商户在平台上创建后会给商户一个id 用来代表这个商户
     * 创建失败则为-1
     */
    private Integer id;
}
