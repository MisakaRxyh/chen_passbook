package com.chen.passbook.merchants.vo;

import com.chen.passbook.merchants.constant.ErrorCode;
import com.chen.passbook.merchants.dao.MerchantsDao;
import com.chen.passbook.merchants.entity.Merchants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 投放的优惠券对象定义
 *
 * @author Chen on 2021/8/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassTemplate {
    /**
     * 所属商户id
     */
    private Integer id;

    /**
     * 优惠券标题
     */
    private String title;

    /**
     * 优惠券摘要
     */
    private String summary;

    /**
     * 优惠券详细信息
     */
    private String desc;

    /**
     * 最大的个数限制
     */
    private Long limit;

    /**
     * 优惠券是否有Token，用于商户核销
     */
    private Boolean hasToken; // token 存储于 Redis Set 中，每次领取从 Redis 中获取

    /**
     * 优惠券背景色
     */
    private Integer background;

    /**
     * 优惠券开始时间
     */
    private Date start;

    /**
     * 优惠券结束时间
     */
    private Date end;

    /**
     * 校验优惠券对象的有效性
     * @param merchantsDao
     * @return
     */
    public ErrorCode validate(MerchantsDao merchantsDao){
        if(null == merchantsDao.findById(id)){
            return ErrorCode.MERCHANTS_NOT_EXIST;
        }
        return ErrorCode.SUCCESS;
    }
}
