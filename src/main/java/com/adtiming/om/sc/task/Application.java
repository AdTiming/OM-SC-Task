package com.adtiming.om.sc.task;

import com.adtiming.om.sc.task.dto.NodeConfig;
import com.adtiming.om.sc.task.service.AppConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@SpringBootApplication(scanBasePackages = "com.adtiming.om.sc.task")
@EnableScheduling
public class Application {

    private static final Logger LOG = LogManager.getLogger();

    private Map<Integer, NodeConfig> nodeConfigs;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public CloseableHttpAsyncClient httpAsyncClient() {
        return HttpAsyncClients.custom()
                .setMaxConnPerRoute(50000)
                .setMaxConnTotal(100000)
                .setUserAgent("om-server/1.0.1")
                .build();
    }

    @Bean
    public ResponseContentEncoding responseContentEncoding() {
        return new ResponseContentEncoding();
    }

    @Bean
    public NodeConfig nc(@Autowired AppConfig cfg,
                         @Autowired ObjectMapper objectMapper) {
        try {
            String nodeid;
            Path nodeidPath = Paths.get("data/nodeid");
            if (Files.exists(nodeidPath)) {
                nodeid = new String(Files.readAllBytes(nodeidPath), UTF_8);
            } else {
                nodeid = UUID.randomUUID().toString();
                if (Files.notExists(nodeidPath.getParent())) {
                    Files.createDirectories(nodeidPath.getParent());
                }
                Files.write(nodeidPath, nodeid.getBytes(UTF_8));
            }
            cfg.setNodeId(nodeid);

            String url = String.format("http://%s:19012/sc/config/list?nodeid=%s", cfg.getDtask(), nodeid);
            JSONObject object = objectMapper.readValue(new URL(url), JSONObject.class);
            if (object != null && !object.isEmpty()) {
                JSONArray list = object.getJSONArray("data");
                if (!CollectionUtils.isEmpty(list)) {
                    nodeConfigs = new HashMap<>(list.size());
                    for (int i = 0; i < list.size(); i++) {
                        NodeConfig nc = list.getObject(i, NodeConfig.class);
                        nodeConfigs.put(nc.getDcenter(), nc);
                    }

                    NodeConfig nc = nodeConfigs.get(cfg.getDcenter());
                    LOG.info("OM-SC-TASK init, dc: {}, dtask: {}, {}", cfg.getDcenter(), cfg.getDtask(), nc);
                    return nc;
                }
            }
        } catch (Exception e) {
            LOG.error("load sc/list from dtask error", e);
            System.exit(1);
        }
        return new NodeConfig();
    }

    @Bean
    public Map<Integer, NodeConfig> nodeConfigs() {
        return nodeConfigs;
    }

}
