package com.adtiming.om.sc.task.web;

import com.adtiming.om.sc.task.service.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * API for Dc
 */
@Controller
public class APIController {

    private static final Logger log = LogManager.getLogger();

    @Resource
    private AppConfig cfg;

    @GetMapping({"/", "index.html"})
    public String index(HttpServletRequest req) {
        req.setAttribute("dc", cfg.getDcenter());
        return "index";
    }
}
