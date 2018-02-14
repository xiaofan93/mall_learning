package com.mmall.controller.portal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author fan
 * @date 2018/2/2 21:10
 */
@Controller
@RequestMapping("/test/")
@Slf4j
public class TestController {

    public static void main(String[] args) {

        System.out.println("test111111111111111");
    }

    @RequestMapping("test.do")
    @ResponseBody
    public String test(String str) {
        log.info("testinfo");
        log.error("errortest");
        return "string"+str;
    }
}
