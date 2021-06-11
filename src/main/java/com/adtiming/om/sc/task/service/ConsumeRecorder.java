package com.adtiming.om.sc.task.service;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Component
public class ConsumeRecorder {

    private static final Logger log = LogManager.getLogger();

    // server,group_id,topic,partition,offset
    private Map<String, Map<String, Map<String, Map<Integer, Long>>>> consumeRecord;
    private final Path consumeRecordFile = Paths.get("data/consume_record");

    @PostConstruct
    private void init() {
        File dir = new File("data");
        if (!dir.exists() && dir.mkdir())
            log.info("mkdir {}", dir);
        consumeRecord = new HashMap<>();
        if (Files.exists(consumeRecordFile)) {
            try (Stream<String> stream = Files.lines(consumeRecordFile)) {
                stream.forEach(line -> {
                    String[] s = line.split(",", -1);
                    if (s.length < 5) return;
                    consumeRecord.computeIfAbsent(s[0], k -> new ConcurrentHashMap<>())
                            .computeIfAbsent(s[1], k -> new ConcurrentHashMap<>())
                            .computeIfAbsent(s[2], k -> new ConcurrentHashMap<>())
                            .put(NumberUtils.toInt(s[3]), NumberUtils.toLong(s[4]));
                });
            } catch (Exception e) {
                log.error("read record error", e);
            }
        }
    }

    @PreDestroy
    @Scheduled(cron = "0 * * * * ?")
    public void saveConsumeRecord() {
        try (BufferedWriter out = Files.newBufferedWriter(consumeRecordFile, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, Map<String, Map<String, Map<Integer, Long>>>> m0 : consumeRecord.entrySet()) {
                for (Map.Entry<String, Map<String, Map<Integer, Long>>> m1 : m0.getValue().entrySet()) {
                    for (Map.Entry<String, Map<Integer, Long>> m2 : m1.getValue().entrySet()) {
                        for (Map.Entry<Integer, Long> m3 : m2.getValue().entrySet()) {
                            out.append(m0.getKey()).append(',')
                                    .append(m1.getKey()).append(',')
                                    .append(m2.getKey()).append(',')
                                    .append(String.valueOf(m3.getKey())).append(',')
                                    .append(String.valueOf(m3.getValue())).append('\n');
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("write record error", e);
        }
    }

    public boolean hasConsumed(String server, String groupId, ConsumerRecord<?, ?> r) {
        Map<Integer, Long> pi = consumeRecord
                .computeIfAbsent(server, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(groupId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(r.topic(), k -> new ConcurrentHashMap<>());
        Long last_offset = pi.get(r.partition());
        long cur_offset = r.offset();
        if (cur_offset == 0 && last_offset != null)
            log.error("kafka offset reset from begining, server:{},group:{},topic:{},partition:{}", server, groupId, r.topic(), r.partition());
        if (last_offset != null && cur_offset > 2 && last_offset > cur_offset) {
            log.warn("offset less than last_offset, server: {}, group_id: {}, topic: {}, partition: {}, offset: {}, last_offset: {}",
                    server, groupId, r.topic(), r.partition(), r.offset(), last_offset);
            return true;
        }
        pi.put(r.partition(), r.offset());
        return false;
    }

    public Long getTopicOffset(String server, String groupId, String topic, int partition) {
        return consumeRecord
                .getOrDefault(server, Collections.emptyMap())
                .getOrDefault(groupId, Collections.emptyMap())
                .getOrDefault(topic, Collections.emptyMap())
                .getOrDefault(partition, 0L);
    }
}
