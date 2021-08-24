package com.chen.passbook.merchants.dao;

import com.chen.passbook.merchants.entity.Merchants;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Merchants Dao 接口
 */
public interface MerchantsDao extends JpaRepository<Merchants, Integer> {
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
}
