package com.chen.passbook.passbook.service.impl;

import com.chen.passbook.passbook.constant.Constants;
import com.chen.passbook.passbook.dao.MerchantsDao;
import com.chen.passbook.passbook.entity.Merchants;
import com.chen.passbook.passbook.mapper.PassTemplateRowMapper;
import com.chen.passbook.passbook.service.IInventoryService;
import com.chen.passbook.passbook.service.IUserPassService;
import com.chen.passbook.passbook.utils.RowKeyGenUtil;
import com.chen.passbook.passbook.vo.*;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.LongComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 获取库存信息，只返回用户没有领取的
 *
 * @author Chen on 2021/8/23
 */
@Slf4j
@Service
public class InventoryServiceImpl implements IInventoryService {

    /**
     * HBase 客户端
     */
    private final HbaseTemplate hbaseTemplate;

    /**
     * Merchants Dao
     */
    private final MerchantsDao merchantsDao;

    /**
     *
     */
    private final IUserPassService userPassService;

    @Autowired
    public InventoryServiceImpl(HbaseTemplate hbaseTemplate, MerchantsDao merchantsDao, IUserPassService userPassService) {
        this.hbaseTemplate = hbaseTemplate;
        this.merchantsDao = merchantsDao;
        this.userPassService = userPassService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response getInventoryInfo(Long userId) throws Exception {

        // 通过 userId 获取该用户的全部优惠券，需要将这些优惠券从库存展示中去掉
        Response allUserPass = userPassService.getUserAllPassInfo(userId);
        List<PassInfo> passInfos = (List<PassInfo>) allUserPass.getData();

        // 获取这些优惠券的优惠券模板信息
        List<PassTemplate> excludeObject = passInfos.stream().map(
                PassInfo::getPassTemplate
        ).collect(Collectors.toList());

        // 将这些需要排除的优惠券的模板 Id 保存到列表中
        // 注意：优惠券模板的 Id 是根据 优惠券所属商户Id+优惠券标题构成 是唯一值
        List<String> excludeIds = new ArrayList<>();
        excludeObject.forEach(e -> excludeIds.add(RowKeyGenUtil.genPassTemplateRowKey(e)));

        List<PassTemplate> availablePassTemplate = getAvailablePassTemplate(excludeIds);
        List<PassTemplateInfo> passTemplateInfos = buildPassTemplateInfo(availablePassTemplate);
        InventoryResponse inventoryResponse = new InventoryResponse(userId, passTemplateInfos);

        return new Response(inventoryResponse);
    }

    /**
     * 获取系统中可用的优惠券
     *
     * @param excludesIds 需要排除的优惠券Ids，即用户已经领取的优惠券Ids
     * @return
     */
    private List<PassTemplate> getAvailablePassTemplate(List<String> excludesIds) {
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.GREATER,
                        new LongComparator(0L)
                )
        );
        filterList.addFilter(
                new SingleColumnValueFilter(
                        Bytes.toBytes(Constants.PassTemplateTable.FAMILY_C),
                        Bytes.toBytes(Constants.PassTemplateTable.LIMIT),
                        CompareFilter.CompareOp.EQUAL,
                        new LongComparator(-1)
                )
        );

        Scan scan = new Scan();
        scan.setFilter(filterList);

        List<PassTemplate> validTemplates = hbaseTemplate.find(
                Constants.PassTemplateTable.TABLE_NAME, scan, new PassTemplateRowMapper()
        );
        List<PassTemplate> availablePassTemplates = new ArrayList<>();

        Date cur = new Date();

        for (PassTemplate validTemplate : validTemplates) {
            if (excludesIds.contains(RowKeyGenUtil.genPassTemplateRowKey(validTemplate))) {
                continue;
            }
            if (cur.getTime() >= validTemplate.getStart().getTime()
                    && cur.getTime() <= validTemplate.getEnd().getTime()) {
                availablePassTemplates.add(validTemplate);
            }
        }
        return availablePassTemplates;
    }

    /**
     * 构造优惠券的信息
     * PassTemplateInfo = PassTemplate + Merchants
     *
     * @param passTemplates
     * @return
     */
    private List<PassTemplateInfo> buildPassTemplateInfo(List<PassTemplate> passTemplates) {
        Map<Integer, Merchants> merchantsMap = new HashMap<>();
        List<Integer> merchantsIds = passTemplates.stream().map(
                PassTemplate::getId
        ).collect(Collectors.toList());
        List<Merchants> merchants = merchantsDao.findByIdIn(merchantsIds);
        merchants.forEach(m -> merchantsMap.put(m.getId(),m));

        List<PassTemplateInfo> result = new ArrayList<>(passTemplates.size());

        for (PassTemplate passTemplate : passTemplates){
            Merchants mc = merchantsMap.getOrDefault(passTemplate.getId(),null);
            if (null == mc){
                log.error("Merchants Error: {}", passTemplate.getId());
                continue;
            }
            result.add(new PassTemplateInfo(passTemplate, mc));
        }
        return result;
    }
}
