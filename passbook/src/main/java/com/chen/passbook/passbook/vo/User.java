package com.chen.passbook.passbook.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Object
 *
 * @author Chen on 2021/8/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户基本信息 b列族
     */
    private BaseInfo baseInfo;

    /**
     * 用户额外信息 o列族
     */
    private OtherInfo otherInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseInfo {
        private String name;
        private Integer age;
        private String sex;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtherInfo {
        private String phone;
        private String address;
    }
}
