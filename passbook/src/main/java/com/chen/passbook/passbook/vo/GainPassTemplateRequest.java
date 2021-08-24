package com.chen.passbook.passbook.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户领取优惠券的请求对象
 *
 * @author Chen on 2021/8/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GainPassTemplateRequest {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * PassTemplate 对象
     */
    private PassTemplate passTemplate;


}
