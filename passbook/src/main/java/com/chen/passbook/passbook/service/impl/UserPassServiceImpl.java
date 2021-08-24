package com.chen.passbook.passbook.service.impl;

import com.alibaba.fastjson.JSON;
import com.chen.passbook.passbook.constant.Constants;
import com.chen.passbook.passbook.constant.PassStatus;
import com.chen.passbook.passbook.dao.MerchantsDao;
import com.chen.passbook.passbook.entity.Merchants;
import com.chen.passbook.passbook.mapper.PassRowMapper;
import com.chen.passbook.passbook.service.IUserPassService;
import com.chen.passbook.passbook.vo.Pass;
import com.chen.passbook.passbook.vo.PassInfo;
import com.chen.passbook.passbook.vo.PassTemplate;
import com.chen.passbook.passbook.vo.Response;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户优惠券相关功能实现
 *
 * @author Chen on 2021/8/22
 */
@Slf4j
@Service
public class UserPassServiceImpl implements IUserPassService {

    /**
     * HBase 客户端
     */
    private final HbaseTemplate hbaseTemplate;

    /**
     * MerchantsDao
     */
    private final MerchantsDao merchantsDao;

    @Autowired
    public UserPassServiceImpl(HbaseTemplate hbaseTemplate, MerchantsDao merchantsDao) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
    }

    @Override
    public Response getUserPassInfo(Long userId) throws Exception {
        Response passInfoByStatus = getPassInfoByStatus(userId, PassStatus.UNUSED);
        return passInfoByStatus;
    }

    @Override
    public Response getUserUsedPassInfo(Long userId) throws Exception {
        Response passInfoByStatus = getPassInfoByStatus(userId, PassStatus.UNUSED);
        return passInfoByStatus;
    }

    @Override
    public Response getUserAllPassInfo(Long userId) throws Exception {
        Response passInfoByStatus = getPassInfoByStatus(userId, PassStatus.ALL);
        return passInfoByStatus;
    }

    @Override
    public Response userUsePass(Pass pass) {

        // 根据 userId 构造行键前缀, 根据行键前缀在 HBase 找到用户对应的优惠券
        byte[] rowPrefix = Bytes.toBytes(new StringBuilder(String.valueOf(pass.getUserId())).reverse().toString());

        Scan scan = new Scan();
        List<Filter> filters = new ArrayList<>();
        // 找到以 userId 相关为前缀的优惠券记录
        filters.add(new PrefixFilter(rowPrefix));
        // 找到 与 用户拥有的优惠券 Pass 的 templateId 相同的优惠券记录
        filters.add(new SingleColumnValueFilter(
                Constants.PassTable.FAMILY_I.getBytes(),
                Constants.PassTable.TEMPLATE_ID.getBytes(),
                CompareFilter.CompareOp.EQUAL,
                Bytes.toBytes(pass.getTemplateId())
        ));
        // 找到 与 用户拥有的优惠券 Pass 的优惠券记录中其中没有过期的优惠券记录
        filters.add(new SingleColumnValueFilter(
                Constants.PassTable.FAMILY_I.getBytes(),
                Constants.PassTable.CON_DATE.getBytes(),
                CompareFilter.CompareOp.EQUAL,
                Bytes.toBytes("-1")
        ));

        scan.setFilter(new FilterList(filters));
        List<Pass> passes = hbaseTemplate.find(Constants.PassTable.TABLE_NAME, scan, new PassRowMapper());

        if (null == passes || passes.size() != 1) {
            log.error("UserUsePass Error: {}", JSON.toJSONString(pass));
            return Response.failure("UserUserPass Error");
        }

        byte[] FAMILY_I = Constants.PassTable.FAMILY_I.getBytes();
        byte[] CON_DATE = Constants.PassTable.CON_DATE.getBytes();

        List<Mutation> datas = new ArrayList<>();
        // 获取记录的行键
        Put put = new Put(passes.get(0).getRowKey().getBytes());
        // 修改 CON_DATE, 将消费时间从 -1 改为当前时间
        put.addColumn(FAMILY_I, CON_DATE, Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(new Date())));
        datas.add(put);

        hbaseTemplate.saveOrUpdates(Constants.PassTable.TABLE_NAME, datas);

        return Response.success();
    }

    /**
     * 根据优惠券状态，获取优惠券信息
     *
     * @param userId
     * @param status
     * @return
     * @throws Exception
     */
    private Response getPassInfoByStatus(Long userId, PassStatus status) throws Exception {
        // 根据 userId 构造行键前缀
        byte[] rowPrefix = Bytes.toBytes(new StringBuilder(String.valueOf(userId)).reverse().toString());
        // 就是根据传进来的 status 来筛选 USED 和 UNUSED 的优惠券记录
        // 如果 status == USED 则 compareOp 为 !=
        // 如果 status == UNUSED 则 compareOp 为 ==
        CompareFilter.CompareOp compareOp =
                status == PassStatus.UNUSED ?
                        CompareFilter.CompareOp.EQUAL : CompareFilter.CompareOp.NOT_EQUAL;
        Scan scan = new Scan();
        List<Filter> filters = new ArrayList<>();

        // 1、行键前缀过滤器，找到特定用户的优惠券
//        scan.setFilter(new PrefixFilter(rowPrefix));
        filters.add(new PrefixFilter(rowPrefix));
        // 2、基于列单元值的过滤器，找到未使用的优惠券
        if (status != PassStatus.ALL) {
            filters.add(new SingleColumnValueFilter(
                    Constants.PassTable.FAMILY_I.getBytes(),
                    Constants.PassTable.CON_DATE.getBytes(),
                    // 这里相当于筛选出 CON_DATE 字段 != -1 或 == -1 的记录
                    compareOp, Bytes.toBytes(-1)
            ));
        }
        scan.setFilter(new FilterList(filters));
        // 不能设置两次过滤器 后面的会将前面的覆盖掉
//            scan.setFilter(
//                    new SingleColumnValueFilter(
//                            Constants.PassTable.FAMILY_I.getBytes(),
//                            Constants.PassTable.CON_DATE.getBytes(),
//                            // 这里相当于筛选出 CON_DATE 字段 != -1 或 == -1 的记录
//                            compareOp, Bytes.toBytes(-1)
//                    ));


        // 通过 HBase 查询到 满足 scan 条件的记录，并根据 PassRowMapper 将其转化为 Pass 对象
        List<Pass> passes = hbaseTemplate.find(Constants.PassTable.TABLE_NAME, scan, new PassRowMapper());
        // 通过 Passes 对象 构造 < PassTemplate 在 HBase 中的 RowKey , PassTemplate >
        Map<String, PassTemplate> passTemplateMap = buildPassTemplateMap(passes);
        // 通过上面的 PassTemplate Map 的 values 方法获取到值 即 PassTemplate List 再通过 List 中的 商户Id 查询到对应的商户信息
        Map<Integer, Merchants> merchantsMap = buildMerchantsMap(new ArrayList<>(passTemplateMap.values()));

        // PassInfo 是 Pass（用户领取的优惠券）+ PassTemplate（优惠券模板）+ Merchants（商户信息）的整合
        // PassInfo 包含了一条优惠券的所有相关信息
        List<PassInfo> result = new ArrayList<>();

        // 通过遍历 用户领取的优惠券 Pass 获取到其他两个对象 并填充到一个 PassInfo 对象中
        for (Pass pass : passes) {
            PassTemplate passTemplate = passTemplateMap.getOrDefault(
                    pass.getTemplateId(), null);
            if (null == passTemplate) {
                log.error("PassTemplate Null : {}", pass.getTemplateId());
                continue;
            }

            Merchants merchants = merchantsMap.getOrDefault(passTemplate.getId(), null);
            if (null == merchants) {
                log.error("Merchants Null : {}", passTemplate.getId());
                continue;
            }
            result.add(new PassInfo(pass, passTemplate, merchants));
        }
        return new Response(result);
    }

    /**
     * 通过获取的 Passes 对象构造 Map
     * 通过用户领取的优惠券 Pass 对象 获取到 优惠券模板对象 即优惠券的详细信息
     *
     * @param passes
     * @return
     * @throws Exception
     */
    private Map<String, PassTemplate> buildPassTemplateMap(List<Pass> passes) throws Exception {

        String[] patterns = new String[]{"yyyy-MM-dd"};
        byte[] FAMILY_B = Bytes.toBytes(Constants.PassTemplateTable.FAMILY_B);
        byte[] ID = Bytes.toBytes(Constants.PassTemplateTable.ID);
        byte[] TITLE = Bytes.toBytes(Constants.PassTemplateTable.TITLE);
        byte[] SUMMARY = Bytes.toBytes(Constants.PassTemplateTable.SUMMARY);
        byte[] DESC = Bytes.toBytes(Constants.PassTemplateTable.DESC);
        byte[] HAS_TOKEN = Bytes.toBytes(Constants.PassTemplateTable.HAS_TOKEN);
        byte[] BACKGROUND = Bytes.toBytes(Constants.PassTemplateTable.BACKGROUND);

        byte[] FAMILY_C = Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C);
        byte[] LIMIT = Bytes.toBytes(Constants.PassTemplateTable.LIMIT);
        byte[] START = Bytes.toBytes(Constants.PassTemplateTable.START);
        byte[] END = Bytes.toBytes(Constants.PassTemplateTable.END);

        // 相当于遍历 passes ，获取 templateId 保存到 templateIds 中
        // :: 方法引用
        List<String> templateIds = passes.stream().map(
                Pass::getTemplateId
        ).collect(Collectors.toList());

        List<Get> templateGets = new ArrayList<>(templateIds.size());
        templateIds.forEach(t -> templateGets.add(new Get(Bytes.toBytes(t))));

        // 通过 Ids 查询到 HBase 中的 PassTemplate 记录
        Result[] templateResults = hbaseTemplate.getConnection()
                .getTable(TableName.valueOf(Constants.PassTemplateTable.TABLE_NAME))
                .get(templateGets);

        // 构造 PassTemplate -> PassTemplate Object 的 Map，用于构造 PassInfo
        // 将查询出的 PassTemplate 字节数组信息转换为 PassTemplate 对象
        // 并将其以 键值对 的形式保存 Key：PassTemplate 在 HBase 中的 KeyRow Value：PassTemplate
        Map<String, PassTemplate> templateId2Object = new HashMap<>();
        for (Result result : templateResults) {
            PassTemplate passTemplate = new PassTemplate();

            passTemplate.setId(Bytes.toInt(result.getValue(FAMILY_B, ID)));
            passTemplate.setTitle(Bytes.toString(result.getValue(FAMILY_B, TITLE)));
            passTemplate.setSummary(Bytes.toString(result.getValue(FAMILY_B, SUMMARY)));
            passTemplate.setDesc(Bytes.toString(result.getValue(FAMILY_B, DESC)));
            passTemplate.setHasToken(Bytes.toBoolean(result.getValue(FAMILY_B, HAS_TOKEN)));
            passTemplate.setBackground(Bytes.toInt(result.getValue(FAMILY_B, BACKGROUND)));
            passTemplate.setLimit(Bytes.toLong(result.getValue(FAMILY_C, LIMIT)));
            passTemplate.setStart(DateUtils.parseDate(Bytes.toString(result.getValue(FAMILY_B, START)), patterns));
            passTemplate.setEnd(DateUtils.parseDate(Bytes.toString(result.getValue(FAMILY_B, END)), patterns));

            templateId2Object.put(Bytes.toString(result.getRow()), passTemplate);
        }

        return templateId2Object;
    }

    /**
     * 通过获取的 PassTemplate 构造 Merchant Map
     * 通过 优惠券模板 获取到 商户 的信息
     *
     * @param passTemplates
     * @return
     */
    private Map<Integer, Merchants> buildMerchantsMap(List<PassTemplate> passTemplates) {

        Map<Integer, Merchants> merchantsMap = new HashMap<>();
        List<Integer> merchantsIds = passTemplates.stream().map(
                PassTemplate::getId
        ).collect(Collectors.toList());
        List<Merchants> merchants = merchantsDao.findByIdIn(merchantsIds);

        merchants.forEach(m -> merchantsMap.put(m.getId(), m));

        return merchantsMap;
    }
}
