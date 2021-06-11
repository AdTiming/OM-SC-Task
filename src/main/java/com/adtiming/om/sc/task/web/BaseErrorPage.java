package com.adtiming.om.sc.task.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/error")
public class BaseErrorPage extends AbstractErrorController {

    /**
     * Create a new {@link BaseErrorPage} instance.
     *
     * @param errorAttributes    the error attributes
     * @param errorViewResolvers error view resolvers
     */
    public BaseErrorPage(@Autowired ErrorAttributes errorAttributes,
                         @Autowired List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorViewResolvers);
    }

    @Deprecated
    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping
    public ResponseEntity<?> error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NOT_FOUND)
            return ResponseEntity.ok(""); // 防遍历, 404 转 200
        return new ResponseEntity<>(body, status);
    }

}
