package com.chen.passbook.passbook.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 库存请求响应
 * 商户在平台上投放的可用优惠券
 *
 * @author Chen on 2021/8/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    /**
     * 用户id
     * 标识不同的用户应该看到不同的优惠券库存，已领取的优惠券不应该看到
     */
    private Long userId;

    /**
     * 优惠券模板信息：在库存中，用户没有领取且没有过期的优惠券信息
     */
    private List<PassTemplateInfo> passTemplateInfos;

}
