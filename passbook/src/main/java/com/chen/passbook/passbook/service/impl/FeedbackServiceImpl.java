package com.chen.passbook.passbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.chen.passbook.passbook.constant.Constants;
import com.chen.passbook.passbook.mapper.FeedbackRowMapper;
import com.chen.passbook.passbook.service.IFeedbackService;
import com.chen.passbook.passbook.utils.RowKeyGenUtil;
import com.chen.passbook.passbook.vo.Feedback;
import com.chen.passbook.passbook.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评论功能实现
 *
 * @author Chen on 2021/8/22
 */
@Slf4j
@Service
public class FeedbackServiceImpl implements IFeedbackService {

    private final HbaseTemplate hbaseTemplate;

    @Autowired
    public FeedbackServiceImpl(HbaseTemplate hbaseTemplate) {
        this.hbaseTemplate = hbaseTemplate;
    }

    @Override
    public Response createFeedback(Feedback feedback) {
        if (!feedback.validate()) {
            log.error("Feedback Error: {}", JSON.toJSONString(feedback));
            return Response.failure("Feedback Error");
        }
        Put put = new Put(Bytes.toBytes(RowKeyGenUtil.genFeedbackRowKey(feedback)));

        put.addColumn(
                Bytes.toBytes(Constants.Feedback.FAMILY_I),
                Bytes.toBytes(Constants.Feedback.USER_ID),
                Bytes.toBytes(feedback.getUserId())
        );
        put.addColumn(
                Bytes.toBytes(Constants.Feedback.FAMILY_I),
                Bytes.toBytes(Constants.Feedback.TYPE),
                Bytes.toBytes(feedback.getType())
        );
        put.addColumn(
                Bytes.toBytes(Constants.Feedback.FAMILY_I),
                Bytes.toBytes(Constants.Feedback.TEMPLATE_ID),
                Bytes.toBytes(feedback.getTemplateId())
        );
        put.addColumn(
                Bytes.toBytes(Constants.Feedback.FAMILY_I),
                Bytes.toBytes(Constants.Feedback.COMMENT),
                Bytes.toBytes(feedback.getComment())
        );

        hbaseTemplate.saveOrUpdate(Constants.Feedback.TABLE_NAME, put);

        return Response.success();
    }

    @Override
    public Response getFeedback(Long userId) {

        byte[] reverseUserId = new StringBuilder(String.valueOf(userId)).reverse().toString().getBytes();

        // 设置扫描器
        Scan scan = new Scan();
        // 设置前缀过滤器，以翻转后的userId作为前缀
        scan.setFilter(new PrefixFilter(reverseUserId));

        // 查询 HBase，在 Constants.Feedback.TABLE_NAME 表下通过 scan 查询，查询出来的结果通过 FeedbackRowMapper 映射出 Feedback 对象
        // get 与 find 的区别：get 只会获取一条，且如果没找到会抛出异常，find 获取全部，没找到返回null
        List<Feedback> feedbacks = hbaseTemplate.find(Constants.Feedback.TABLE_NAME, scan, new FeedbackRowMapper());

        return new Response(feedbacks);
    }
}
