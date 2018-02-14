package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author fan
 * @date 2018/2/14 14:56
 */
@Slf4j
public class CookieUtil {
    private final static String COOKIE_DOMAIN = ".happymmall.com";
    private final static String COOKIE_NAME = "mmall_login_token";

    public static void writeLoginToken(HttpServletResponse response,String token) {
        Cookie ck = new Cookie(COOKIE_NAME,token);
        ck.setDomain(COOKIE_DOMAIN);
        ck.setPath("/");  //代表设置在根目录
        //单位秒，如果不设置maxAge，cookie就不会写入硬盘，而是写在内存，只在当前页面有效
        //如果设置成-1，就代表永久有效
        ck.setMaxAge(60 * 60 * 24 * 365);
        ck.setHttpOnly(true);
        log.info("write cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
        response.addCookie(ck);
    }

    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cks = request.getCookies();
        if (cks != null) {
            for (Cookie ck : cks) {
                log.info("readCookie cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                if (StringUtils.equals(ck.getName(),COOKIE_NAME)) {
                    log.info("return cookieName:{},cookieValue:{}",ck.getName(),ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    public static void delLoginToken(HttpServletRequest request,HttpServletResponse response) {
         Cookie[] cks = request.getCookies();
         if (cks != null) {
             for (Cookie ck :cks) {
                 if (StringUtils.equals(ck.getName(),COOKIE_NAME)) {
                     ck.setDomain(COOKIE_DOMAIN);
                     ck.setPath("/");
                     ck.setHttpOnly(true);
                     ck.setMaxAge(0);   //设置成0，表示删除此cookie
                     response.addCookie(ck);
                     return; //结束循环
                 }
             }
         }
    }
}
