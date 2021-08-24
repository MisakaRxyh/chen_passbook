package com.chen.passbook.passbook.utils;

import com.chen.passbook.passbook.vo.Feedback;
import com.chen.passbook.passbook.vo.GainPassTemplateRequest;
import com.chen.passbook.passbook.vo.PassTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * RowKey 生成器工具类
 *
 * @author Chen on 2021/8/21
 */
@Slf4j
public class RowKeyGenUtil {

    /**
     * 根据提供的 PassTemplate 对象生成 RowKey
     *
     * @param passTemplate
     * @return
     */
    public static String genPassTemplateRowKey(PassTemplate passTemplate) {

        String passInfo = String.valueOf(passTemplate.getId() + "_" + passTemplate.getTitle());
        /**
         * 为什么要md5处理passInfo转为rowKey？
         * 因为HBase是一个集群，HBase上的数据都是基于RowKey进行存储，RowKey相近的值会存在一起，
         * 如果不处理，数据会集中在一个节点，也就是一台机器上，不利于负载均衡，
         * RowKey越分散，数据存放会越分散，有利于HBase负载均衡的策略，查询速度会更快
         */
        String rowKey = DigestUtils.md5Hex(passInfo);

        log.info("GenPassTemplateRowKey:", passInfo, rowKey);
        return rowKey;
    }

    /**
     * 根据 Feedback 构造 RowKey
     *
     * @param feedback
     * @return
     */
    public static String genFeedbackRowKey(Feedback feedback) {
        /**
         * 对于每一个feedback来说都会有一个用户id，而对于同一个用户来说，他的所有的feedback存在相近的位置会比较好，
         * 这样有利于我们去查询扫码同一个用户的所有feedback
         * reverse()：是因为系统中的userId的前缀与系统中用户个数相关，数据量大了之后，前缀是相同的，而后缀是一个随机数，翻转后有利于数据的分散
         * 而(Long.MAX_VALUE - System.currentTimeMillis()则是使得数据根据创建时间倒序存放在HBase中，这样扫描HBase最先拿到的是用户最近创建的一条feedback
         *
         */
        return new StringBuilder(String.valueOf(feedback.getUserId())).reverse().toString() +
                (Long.MAX_VALUE - System.currentTimeMillis());
    }

    /**
     * 根据提供的领取优惠券请求生成 RowKey ，只可以在领取优惠券的时候使用
     * Pass RowKey = reversed(userId) + inverse(timestamp) + PassTemplate RowKey
     * @param request
     * @return
     */
    public static String genPassRowKey(GainPassTemplateRequest request) {
        return new StringBuilder(String.valueOf(request.getUserId())).reverse().toString() +
                (Long.MAX_VALUE - System.currentTimeMillis()) +
                genPassTemplateRowKey(request.getPassTemplate());
    }
}
