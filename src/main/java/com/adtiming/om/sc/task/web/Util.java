package com.adtiming.om.sc.task.web;

import javax.servlet.http.HttpServletRequest;

public abstract class Util {

//    private static final Logger log = LogManager.getLogger();

    private Util() {
    }

    public static String getClientIP(HttpServletRequest req) {
        String remote_ip = req.getHeader("X-Real-IP");
        return remote_ip == null ? req.getRemoteAddr() : remote_ip;
//        String xff = req.getHeader("X-Forwarded-For");
//        log.debug("real_ip: {}, xff: {}", remote_ip, xff);
//        if (StringUtils.isNotBlank(xff)) {
//            return StringUtils.trim(xff.split(",")[0]);
//        } else
//            return remote_ip;
    }

}
