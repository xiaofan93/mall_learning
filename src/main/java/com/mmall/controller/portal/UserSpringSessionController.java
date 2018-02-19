package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisSharedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author fan
 * @date 2018/1/24 21:08
 */

@Controller
@RequestMapping("/user/springsession/")
public class UserSpringSessionController {

    @Autowired
    //此时注入的是接口
    private IUserService iUserService;

    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse) {
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER,response.getData());
            //把cookie信息写入到response中，返回给浏览器
          //  CookieUtil.writeLoginToken(httpServletResponse,sessions.getId());
            //写入redis
           // RedisSharedPoolUtil.setEx(sessions.getId(), JsonUtil.obj2String(response.getData()),Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    //登出功能
    @RequestMapping(value = "logout.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session,HttpServletRequest request,HttpServletResponse response) {
     //  String loginToken = CookieUtil.readLoginToken(request);
        //删除浏览器中的cookie
        //   CookieUtil.delLoginToken(request,response);
        //删除redis中的key
        //  RedisSharedPoolUtil.del(loginToken);

        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.creatBySuccess();
    }


    //登录状态下获取用户信号
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session,HttpServletRequest request) {
        //读取请求来的cookie信息
    /*   String loginToken = CookieUtil.readLoginToken(request);
       if (StringUtils.isEmpty(loginToken)) {
           return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
       }
       //在redis中获取对应的值
      String userJsonStr = RedisSharedPoolUtil.get(loginToken);
      //反序列化成对象
      User user = JsonUtil.String2Obj(userJsonStr,User.class);*/

      User user = (User)session.getAttribute(Const.CURRENT_USER);

       if (user != null) {
            return ServerResponse.creatBySuccess(user);
       }
          return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
    }





}
