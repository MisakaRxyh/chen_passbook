package com.chen.passbook.passbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.chen.passbook.passbook.constant.Constants;
import com.chen.passbook.passbook.mapper.PassTemplateRowMapper;
import com.chen.passbook.passbook.service.IGainPassTemplateService;
import com.chen.passbook.passbook.utils.RowKeyGenUtil;
import com.chen.passbook.passbook.vo.GainPassTemplateRequest;
import com.chen.passbook.passbook.vo.PassTemplate;
import com.chen.passbook.passbook.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户领取优惠券功能实现
 *
 * @author Chen on 2021/8/23
 */
@Slf4j
@Service
public class GainPassTemplateServiceImpl implements IGainPassTemplateService {

    /**
     * HBase 客户端
     */
    private final HbaseTemplate hbaseTemplate;

    /**
     * redis 客户端
     */
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public GainPassTemplateServiceImpl(HbaseTemplate hbaseTemplate, StringRedisTemplate redisTemplate) {
        this.hbaseTemplate = hbaseTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 领取优惠券
     * 操作的是优惠券模板表 即 PassTemplate 表
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Response gainPassTemplate(GainPassTemplateRequest request) throws Exception {
        // 从 HBase 中获取一个优惠券模板对象
        PassTemplate passTemplate;

        // 获取到请求的 PassTemplate 对象
        // 通过 request 请求中的数据 生成 PassTemplateRowKey
        String passTemplateId = RowKeyGenUtil.genPassTemplateRowKey(request.getPassTemplate());
        try {
            // 根据 PassTemplateRowKey 在 HBase 中拿到对应的 字节数组型数据 并通过 PassTemplateRowMapper 将其转化为 PassTemplate 对象
            passTemplate = (PassTemplate) hbaseTemplate.get(
                    Constants.PassTemplateTable.TABLE_NAME,
                    passTemplateId,
                    new PassTemplateRowMapper()
            );
        } catch (Exception e) {
            log.error("Gain PassTemplate Error: {}", JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("Gain PassTemplate Error!");
        }

        // 当前优惠券是否能被领取
        // 当前优惠券是否超过限制
        if (passTemplate.getLimit() <= 1 && passTemplate.getLimit() != -1) {
            log.error("PassTemplate Limit Max: {}", JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("PassTemplate Limit Max");
        }

        // 当前优惠券是否过期
        Date cur = new Date();
        if (!(cur.getTime() >= passTemplate.getStart().getTime() && cur.getTime() <= passTemplate.getEnd().getTime())) {
            log.error("PassTemplate ValidTime Error: {}", JSON.toJSONString(request.getPassTemplate()));
            return Response.failure("PassTemplate ValidTime Error!");
        }

        // 修改剩余限制数
        if (passTemplate.getLimit() != -1) {
            List<Mutation> datas = new ArrayList<>();
            byte[] FAMILY_C = Constants.PassTemplateTable.FAMILY_C.getBytes();
            byte[] LIMIT = Constants.PassTemplateTable.LIMIT.getBytes();

            Put put = new Put(Bytes.toBytes(passTemplateId));
            put.addColumn(
                    FAMILY_C, LIMIT, Bytes.toBytes(passTemplate.getLimit() - 1)
            );
            datas.add(put);

            hbaseTemplate.saveOrUpdates(Constants.PassTemplateTable.TABLE_NAME, datas);
        }

        // 给用户添加优惠券
        // 将优惠券保存到用户优惠券表
        if (!addPassForUser(request, passTemplate.getId(), passTemplateId)) {
            return Response.failure("GainPassTemplate Failure!");
        }

        return Response.success();
    }

    /**
     * 给用户添加优惠券
     *
     * @param request
     * @param merchantsId
     * @param passTemplateId
     * @return
     */
    private boolean addPassForUser(GainPassTemplateRequest request,
                                   Integer merchantsId,
                                   String passTemplateId) throws IOException {

        byte[] FAMILY_I = Constants.PassTable.FAMILY_I.getBytes();
        byte[] USER_ID = Constants.PassTable.USER_ID.getBytes();
        byte[] TEMPLATE_ID = Constants.PassTable.TEMPLATE_ID.getBytes();
        byte[] TOKEN = Constants.PassTable.TOKEN.getBytes();
        byte[] ASSIGNED_DATE = Constants.PassTable.ASSIGNED_DATE.getBytes();
        byte[] CON_DATE = Constants.PassTable.CON_DATE.getBytes();

        List<Mutation> datas = new ArrayList<>();
        // 以请求信息生成 用户的 Pass 对象 的 RowKey
        Put put = new Put(Bytes.toBytes(RowKeyGenUtil.genPassRowKey(request)));
        put.addColumn(FAMILY_I, USER_ID, Bytes.toBytes(request.getUserId()));
        put.addColumn(FAMILY_I, TEMPLATE_ID, Bytes.toBytes(passTemplateId));

        // 获取优惠券模板的request对象中包含了 PassTemplate 对象
        // 判断该 PassTemplate 对象中 hasToken 标志位 是否为 true
        if (request.getPassTemplate().getHasToken()) {
            // 从 redis 中 获取一条 key 为 passTemplateId 的 token
            // pop 操作：从数据库中 弹出一条数据 数据库中会删除这条数据
            String token = redisTemplate.opsForSet().pop(passTemplateId);
            if (null == token) {
                log.error("Token not exist: {}", passTemplateId);
                return false;
            }
            // 将这条 token 数据 保存到 已使用token文件 中
            recordTokenToFile(merchantsId, passTemplateId, token);
            // 给用户的 HBase Pass 对象赋值
            put.addColumn(FAMILY_I, TOKEN, Bytes.toBytes(token));
        } else {
            put.addColumn(FAMILY_I, TOKEN, Bytes.toBytes("-1"));
        }

        // 设置领取时间为当前时间，消费日期为-1，即未消费
        put.addColumn(FAMILY_I, ASSIGNED_DATE, Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(new Date())));
        put.addColumn(FAMILY_I, CON_DATE, Bytes.toBytes("-1"));

        datas.add(put);
        hbaseTemplate.saveOrUpdates(Constants.PassTable.TABLE_NAME, datas);

        return true;
    }

    /**
     * 将已使用的 token 记录到 已使用的token文件 中
     * 用已使用的token文件标记哪些token被使用了
     *
     * @param merchantsId
     * @param passTemplateId
     * @param token
     */
    private void recordTokenToFile(Integer merchantsId, String passTemplateId, String token) throws IOException {
        Files.write(
                // token 的存储文件路径：TOKEN_DIR/merchantsId/passTemplateId
                // 已使用的 token 存储文件路径 TOKEN_DIR/merchantsId/passTemplateId_
                // 在 已使用的token文件 中写如 token记录 并 换行 ，以 APPEND追加 的形式
                // passTemplateId 和 passTemplateId_ 都是一个文件 里面存储了 多条 token
                Paths.get(Constants.TOKEN_DIR, String.valueOf(merchantsId),
                        passTemplateId + Constants.USED_TOKEN_SUFFIX),
                (token + "\n").getBytes(),
                StandardOpenOption.APPEND
        );
    }
}
