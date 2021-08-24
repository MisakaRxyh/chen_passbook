package com.chen.passbook.passbook.service;

import com.chen.passbook.passbook.vo.PassTemplate;

/**
 * PassHBase 服务
 *
 * @author Chen on 2021/8/21
 */
public interface IHBasePassService {
    /**
     * 将 PassTemplate 写入 HBase
     *
     * @param passTemplate
     * @return
     */
    boolean dropPassTemplateToHBase(PassTemplate passTemplate);
}
