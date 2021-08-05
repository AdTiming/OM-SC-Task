package com.adtiming.om.sc.task.service;

import com.adtiming.om.sc.task.dto.NodeConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Service
public class ConsumeService {

    private static final Logger LOG = LogManager.getLogger();

    @Resource
    private AppConfig cfg;

    @Resource
    private NodeConfig nc;

    @Resource
    private Map<Integer, NodeConfig> nodeConfigs;

    @Resource
    private CpCampaignService cpCampaignService;

    @Resource
    private ConsumeRecorder cr;

    private String[] topics;

    @PostConstruct
    private void init() {
        Map<String, Object> props = new HashMap<>();
        props.put(GROUP_ID_CONFIG, cfg.getGroupId());
        props.put(CLIENT_ID_CONFIG, cfg.getGroupId());
        props.put(ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, 3000);
        props.put(SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        if (StringUtils.isNoneBlank(nc.getKafkaServers())) {
            props.put(BOOTSTRAP_SERVERS_CONFIG, nc.getKafkaServers());
            this.topics = new String[]{"cp_campaign_cap" + cfg.getDcenter()};
            for (String topic : topics) {
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
                consumer.subscribe(Collections.singletonList(topic));
                String serverName = nc.getKafkaServers().split(",")[0];
                new Thread(() -> consumerKafka(serverName, cfg.getGroupId(), consumer), "consumer_" + topic).start();
            }
        }

        // 消费除本集群外的其他集群
        List<String> budgetDecrTopic = Collections.singletonList("cp_campaign_cap_decr");
        nodeConfigs.forEach((dc, nodeConfig) -> {
            if (dc == cfg.getDcenter() || StringUtils.isBlank(nodeConfig.getKafkaServers())) {
                return; // 本集群预算已在 track 内扣减, 此处无需处理
            }
            String server = nodeConfig.getKafkaServers();
            String serverName = server.split(",")[0];
            props.put(BOOTSTRAP_SERVERS_CONFIG, server);
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(budgetDecrTopic);
            new Thread(() -> consumerKafka(serverName, cfg.getGroupId(), consumer), "consumer_budget_decr" + dc).start();
        });
    }

    private void consumerKafka(String server, String groupId, KafkaConsumer<String, String> consumer) {
        try {
            while (!cfg.isStoping()) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> r : records) {
                    try {
                        if (cr.hasConsumed(server, groupId, r))
                            continue;
                        String topic = r.topic();
                        if (topic.equals(topics[0])) {// cp_campaign_cap
                            cpCampaignService.incrCap(r);
                        } else if (topic.equals("cp_campaign_cap_decr")) {
                            cpCampaignService.decrCap(r);
                        } else {
                            LOG.error("unknown topic: {}", topic);
                        }
                    } catch (Exception ex) {
                        LOG.error("consume error", ex);
                    }
                }
            }
        } finally {
            consumer.commitSync();
            consumer.close();
        }
    }
}
