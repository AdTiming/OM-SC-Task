package com.adtiming.om.sc.task.service;

import com.adtiming.om.sc.task.dto.NodeConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
public class DCenterService {

    private static final Logger LOG = LogManager.getLogger();

    @Resource
    private AppConfig cfg;

    @Resource
    private ObjectMapper objectMapper;

    private Map<Integer, NodeConfig> nodeConfigs;

    @PostConstruct
    public void init() {
        try {
            String url = String.format("http://%s:19012/snode/config/list?nodeid=%s&dcenter=%d&nc=0",
                    cfg.getDtask(), cfg.getNodeId(), cfg.getDcenter());
            JSONObject object = objectMapper.readValue(new URL(url), JSONObject.class);
            if (object != null && !object.isEmpty()) {
                JSONArray array = object.getJSONArray("data");
                if (array != null && array.size() > 0) {
                    nodeConfigs = new HashMap<>(array.size());
                    for (int i = 0; i < array.size(); i++) {
                        NodeConfig nc = array.getObject(i, NodeConfig.class);
                        nodeConfigs.put(nc.dcenter, nc);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("load snode/list from dtask error", e);
            System.exit(1);
        }
    }

    public Map<Integer, NodeConfig> getDCenterMap() {
        return nodeConfigs;
    }
}
