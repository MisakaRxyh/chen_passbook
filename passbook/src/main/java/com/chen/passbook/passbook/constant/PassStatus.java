package com.chen.passbook.passbook.constant;

/**
 * 优惠券状态枚举
 *
 * @author Chen on 2021/8/21
 */
public enum  PassStatus {
    UNUSED(1, "未被使用的"),
    USED(2,"已经使用的"),
    ALL(3,"全部领取的");

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 状态描述
     */
    private String desc;

    PassStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
