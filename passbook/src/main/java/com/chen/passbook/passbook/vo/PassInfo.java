package com.chen.passbook.passbook.vo;

import com.chen.passbook.passbook.entity.Merchants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户领取的优惠券信息
 *
 * @author Chen on 2021/8/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassInfo {

    /**
     * 优惠券
     */
    private Pass pass;

    /**
     * 优惠券模板
     */
    private PassTemplate passTemplate;

    /**
     * 优惠券对应的商户
     */
    private Merchants merchants;
}
