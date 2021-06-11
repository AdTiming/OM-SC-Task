package com.adtiming.om.sc.task.service;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class CpCampaignService {
    private static final Logger log = LogManager.getLogger();

    private static final DateTimeFormatter KEY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private RedisService jedis;

    void incrCap(ConsumerRecord<String, String> r) {
        try {
            String[] s = r.value().split("\u0001", -1);
            if (s.length < 2) {
                log.warn("{} format error: {}", r.topic(), r.value());
                return;
            }
            String cid = s[0];
            int cap = NumberUtils.toInt(s[1]);

            final LocalTime time = LocalTime.now();
            final int expireSeconds = 86400 - time.toSecondOfDay() + 3600; // 今天系统时区剩余时间秒+1小时
            final String date = LocalDate.now().format(KEY_DATE_FORMAT);
            final String key = String.format("cp_cap_%s_%s", date, cid);

            long surplusCap = jedis.incrBy(key, cap);
            jedis.expire(key, expireSeconds);
            log.debug("incr cp cap, key:{}, cid: {}, cap: {}, surplusCap:{}",  key, cid, cap, surplusCap);
        } catch (Exception e) {
            log.error("incr cp cap to redis error", e);
        }
    }

    void decrCap(ConsumerRecord<String, String> r) {
        try {
            String[] s = r.value().split("\u0001", -1);
            if (s.length < 2) {
                log.warn("{} format error: {}", r.topic(), r.value());
                return;
            }
            String cid = s[0];
            int cap = NumberUtils.toInt(s[1]);

            final LocalTime time = LocalTime.now();
            final int expireSeconds = 86400 - time.toSecondOfDay() + 3600; // 今天系统时区剩余时间秒+1小时
            final String date = LocalDate.now().format(KEY_DATE_FORMAT);
            final String key = String.format("cp_cap_%s_%s", date, cid);

            long surplusCap = jedis.decrBy(key, cap);
            jedis.expire(key, expireSeconds);
            log.debug("decr cp cap, key:{}, cid: {}, cap: {}, surplusCap:{}",  key, cid, cap, surplusCap);
        } catch (Exception e) {
            log.error("decr cp cap to redis error", e);
        }
    }
}
