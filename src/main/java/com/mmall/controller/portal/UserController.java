package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
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
@RequestMapping("/user/")
public class UserController {

    @Autowired
    //此时注入的是接口
    private IUserService iUserService;

    /**
     * 用户登录
     * @param username
     * @param password
     * @param sessions
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession sessions, HttpServletResponse httpServletResponse,HttpServletRequest request) {
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
           // sessions.setAttribute(Const.CURRENT_USER,response.getData());
            CookieUtil.writeLoginToken(httpServletResponse,sessions.getId());
            CookieUtil.readLoginToken(request);
            CookieUtil.delLoginToken(request,httpServletResponse);
        }
        return response;
    }

    //登出功能
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest request,HttpServletResponse response) {
       //  session.removeAttribute(Const.CURRENT_USER);
        CookieUtil.delLoginToken(request,response);
         return ServerResponse.creatBySuccess();
    }

    //注册
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
         return iUserService.register(user);
    }

    //校验
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type) {
        return iUserService.checkValid(str,type);
    }

    //登录状态下获取用户信号
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {

      User user = (User)session.getAttribute(Const.CURRENT_USER);

       if (user != null) {
            return ServerResponse.creatBySuccess(user);
       }
          return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
    }

    //忘记密码，获取问题
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
      return iUserService.selectQuestion(username);
    }

    //校验问题的答案
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer) {
        return iUserService.checkAnswer(username,question,answer);
    }

    //忘记密码中的重置密码
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
         return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    //登录状态下的重置密码
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.creatByErrorMessage("用户未登录");
        }
       return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    //更新用户个人信息
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session,User user) {
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.creatByErrorMessage("用户未登录");
        }
        //用户ID 和用户名是在当前用户中获取的
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response =  iUserService.updateInformation(user);
       if (response.isSuccess()) {
           //返回的更新后的user对象是没有用户名的
           response.getData().setUsername(currentUser.getUsername());
           session.setAttribute(Const.CURRENT_USER,response.getData());
       }
          return response;
    }

    //获取用户信息，如果没有登录就强制登录
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session) {
         User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
         if (currentUser ==null) {
             return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"没有登录需要强制登录");
         }
         return iUserService.getInformation(currentUser.getId());
    }







}
