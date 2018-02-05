package com.mmall.controller.portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author fan
 * @date 2018/2/2 21:10
 */
@Controller
@RequestMapping("/test/")
public class TestController {

    private  static Logger logger = LoggerFactory.getLogger(TestController.class);

    public static void main(String[] args) {

        System.out.println("test111111111111111");
    }

    @RequestMapping("test.do")
    @ResponseBody
    public String test(String str) {
        logger.info("testinfo");
        logger.error("errortest");
        return "string"+str;
    }
}
