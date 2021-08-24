package com.chen.passbook.passbook.dao;

import com.chen.passbook.passbook.entity.Merchants;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Merchants Dao 接口
 *
 * @author Chen on 2021/8/21
 */
public interface MerchantsDao extends JpaRepository<Merchants,Integer> {
    /**
     * 根据id获取商户对象
     * @param id
     * @return
     */
    Merchants findById(Integer id);

    /**
     * 根据商户名称获取商户名称
     * @param name
     * @return
     */
    Merchants findByName(String name);

    /**
     * 根据商户 ids 获取商户对象
     * @param ids
     * @return
     */
    List<Merchants> findByIdIn(List<Integer> ids);
}
