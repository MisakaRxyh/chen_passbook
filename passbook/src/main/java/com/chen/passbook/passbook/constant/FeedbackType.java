package com.chen.passbook.passbook.constant;

/**
 * 用户反馈类型枚举
 *
 * @author Chen on 2021/8/21
 */
public enum FeedbackType {
    PASS(1,"针对优惠券的评论"),
    APP(2,"针对卡包APP的评论");

    /**
     * 评论类型编码
     */
    private Integer code;

    /**
     * 评论类型描述
     */
    private String desc;

    FeedbackType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
