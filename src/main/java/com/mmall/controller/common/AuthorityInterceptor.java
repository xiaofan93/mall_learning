package com.mmall.controller.common;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisSharedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * @author fan
 * @date 2018/2/22 15:29
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       log.info("preHandle");
       //获取请求中Controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        //解析handlerMethod

      String methodName = handlerMethod.getMethod().getName();
      String className = handlerMethod.getBean().getClass().getSimpleName();

      //解析参数
        StringBuffer requestParameterBuffer = new StringBuffer();
        Map parameterMap =request.getParameterMap();
        Iterator it = parameterMap.entrySet().iterator();
        while (it.hasNext()) {
           Map.Entry entry = (Map.Entry)it.next();
           String mapKey = (String) entry.getKey();

           String mapValue = StringUtils.EMPTY;

           //request这个参数的map,里面的value返回的是 string[]
          Object obj = entry.getValue();
          if (obj instanceof String[]) {
              String[] strs = (String[]) obj;
              mapValue = strs.toString();
          }
          requestParameterBuffer.append(mapKey).append("=").append(mapValue);
        }

        //不拦截登录请求
        if (StringUtils.equals(className,"UserManageController") && StringUtils.equals(methodName,"login")) {
            log.info("权限拦截器拦截到请求，className:{} , methodName:{}",className,methodName);
            //拦截登录信息不要打印日志，以免日志泄露，不安全
            return true;
        }

        log.info("权限拦截器拦截到请求，className:{},methodName:{},param:{}",className,methodName,requestParameterBuffer.toString());

        User user = null;

        //判断用户是否登录和是否有管理员权限
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotEmpty(loginToken)) {
            String userJsonStr = RedisSharedPoolUtil.get(loginToken);
            //反序列化
            user = JsonUtil.String2Obj(userJsonStr,User.class);
        }

        if (user == null || (user.getRole().intValue() != Const.Role.ROLE_ADMIN)) {
            //返回false，不会调用controller里面的方法
            //重置response返回给前端
            response.reset();  //这里要添加reset() 方法，否则会报异常 getWriter() has already been called for this response
            response.setCharacterEncoding("UTF-8");  //设置字符集编码，否则会乱码
            response.setContentType("application/json;charset=UTF-8");  //设置返回值的类型
            //打印输出流
            PrintWriter out = response.getWriter();

            //上传由于富文本的控件要求，要特殊处理返回值，这里区分是否登录和是否有权限
            if (user == null) {
                if (StringUtils.equals(className,"ProductManageController") && StringUtils.equals(methodName,"richTestFileUpload")) {
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("message", "请登录管理员");
                    out.print(JsonUtil.obj2String(resultMap));
                }else {
                    out.print(JsonUtil.obj2String(ServerResponse.creatBySuccessMessage("拦截器拦截，用户未登录")));
                }
            }else {
                if (StringUtils.equals(className,"ProductManageController") && StringUtils.equals(methodName,"richTestFileUpload")) {
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("message", "用户无权限操作");
                    out.print(JsonUtil.obj2String(resultMap));
                }else {
                    out.print(JsonUtil.obj2String(ServerResponse.creatBySuccessMessage("拦截器拦截，用户无权限操作")));
                }
            }
            out.flush();  //清空流中数据
            out.close(); //关闭流
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        log.info("afterCompletion");
    }
}
