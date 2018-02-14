package com.mmall.controller.common;

import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author fan
 * @date 2018/2/14 19:00
 */
public class SessionExpireFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
       String loginToken = CookieUtil.readLoginToken(request);

       if (StringUtils.isNotEmpty(loginToken)) {
           //loginToken不为空  获得对应的值
           String userJson = RedisPoolUtil.get(loginToken);
           User user = JsonUtil.String2Obj(userJson,User.class);
           if (user != null) {
               //user不为空 ，重置session的时间，调用expire  session被tomcat默认只有30分钟有效期
               RedisPoolUtil.expire(loginToken, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
           }
       }
       //一个URL被拦截多次，就会形成一个过滤链
       filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
