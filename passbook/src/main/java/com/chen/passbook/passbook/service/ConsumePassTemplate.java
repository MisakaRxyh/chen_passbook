package com.chen.passbook.passbook.service;

import com.alibaba.fastjson.JSON;
import com.chen.passbook.passbook.constant.Constants;
import com.chen.passbook.passbook.vo.PassTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 消费 Kafka 中的 PassTemplate
 *
 * @author Chen on 2021/8/21
 */
@Slf4j
@Component
public class ConsumePassTemplate {
    private final IHBasePassService passService;

    public ConsumePassTemplate(IHBasePassService passService) {
        this.passService = passService;
    }

    /**
     * 解析从 Kafka 消息队列中获取的 PassTemplate ，从字符串转为对象，投放到 HBase 中
     *
     * @param passTemplate
     * @param key
     * @param partition
     * @param topic
     */
    @KafkaListener(topics = {Constants.TEMPLATE_TOPIC})
    public void receive(@Payload String passTemplate,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Consumer Receive PassTemplate: {}", passTemplate);

        PassTemplate pt;
        try {
            pt = JSON.parseObject(passTemplate, PassTemplate.class);
        } catch (Exception e) {
            log.error("Parse PassTemplate Error: {}", e.getMessage());
            return;
        }

        log.info("DropPassTemplateToHBase: {}", passService.dropPassTemplateToHBase(pt));
    }
}
