package com.mmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author fan
 * @date 2018/2/22 11:47
 */
@Slf4j
@Component   //作为spring容器中的bean 注入
public class ExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        //异常信息打印在控制台
        log.error("{} Exception",httpServletRequest.getRequestURI(),e);
        //把异常转换成json格式显示给前端
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());

        //单使用jackson2.0x的时候，使用MappingJackson2JsonView这个类，本项目采用 的是1.9
        modelAndView.addObject("status",ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg","接口异常，请查看服务端后台详细异常信息");
        modelAndView.addObject("data",e.toString());
        return modelAndView;
    }
}
