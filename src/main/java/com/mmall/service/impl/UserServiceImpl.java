package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author fan
 * @date 2018/1/24 21:11
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    public ServerResponse<User> login(String username, String password) {
       int resultCount =  userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.creatByErrorMessage("用户名不存在");
        }
        // MD5密码加密后跟数据库中比较
        String MD5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,MD5Password);
        if (user == null) {
            return  ServerResponse.creatByErrorMessage("密码错误");
        }
        //把登录成功后的密码置空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.creatBySuccess("登录成功",user);
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    public ServerResponse<String> register(User user) {
       ServerResponse validResponse =  this.checkValid(user.getUsername(),Const.USERNAME);
       if (!validResponse.isSuccess()) {
           return validResponse;
       }
        validResponse =  this.checkValid(user.getEmail(),Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        //给用户分组
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5密码加密后在插入数据库
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount =  userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.creatByErrorMessage("注册失败");
        }
        return ServerResponse.creatBySuccessMessage("注册成功");
    }

    /**
     * 校验用户名和密码
     * @param str
     * @param type
     * @return
     */
    public ServerResponse<String> checkValid(String str,String type) {
        if (StringUtils.isNotBlank(type)) {
            //开始进行校验
            if (Const.USERNAME.equals(type)) {
                int resultCount =  userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return  ServerResponse.creatByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return  ServerResponse.creatByErrorMessage("email以存在");
                }
            }
        }else {
            return ServerResponse.creatByErrorMessage("参数错误");
        }
          return ServerResponse.creatBySuccessMessage("校验成功");
    }

    /**
     * 查询问题
     * @param username
     * @return
     */
    public ServerResponse selectQuestion(String username) {
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            //表明该用户不存在
            return ServerResponse.creatByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(username)) {
            return ServerResponse.creatBySuccess(question);
        }
           return ServerResponse.creatByErrorMessage("找回密码的问题是空的");
    }

    /**
     * 校验问题的答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> checkAnswer(String username,String question,String answer) {
       int resultCount = userMapper.checkAnswer(username,question,answer);
       if (resultCount > 0) {
           //说明这个问题和答案是这个用户的，并且正确
           String forgetToken = UUID.randomUUID().toString();
           //添加到本地缓存
           TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
           return ServerResponse.creatBySuccessMessage(forgetToken);
       }
          return ServerResponse.creatByErrorMessage("答案错误");
    }

    /**
     * 忘记密码中的重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
             return ServerResponse.creatByErrorMessage("参数错误，token需要被传递");
        }
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            //表明该用户不存在
            return ServerResponse.creatByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.creatByErrorMessage("token错误或者token以过期");
        }
        if (StringUtils.equals(forgetToken,token)) {
            //把新密码进行MD5加密
           String md5Password =  MD5Util.MD5EncodeUtf8(passwordNew);
          int rowCount =  userMapper.updatePasswordByUsername(username,md5Password);
          if (rowCount > 0) {
              return ServerResponse.creatBySuccessMessage("密码重置成功");
          }
        }else{
            return ServerResponse.creatByErrorMessage("token错误，请重新获取重置密码的token");
        }
             return ServerResponse.creatByErrorMessage("重置密码失败");
    }

    /**
     * 登录下的修改密码
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user) {
        //防止横线越权，一定要校验旧密码是该用户的，如果不指定id 查询出来的结果很可能就是大于0的
       String md5passwordOld =  MD5Util.MD5EncodeUtf8(passwordOld);
       int resultCount =  userMapper.checkPassword(md5passwordOld,user.getId());
       if (resultCount == 0) {
           return ServerResponse.creatByErrorMessage("旧密码错误");
       }
       //把新密码加密
       user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0 ) {
            return ServerResponse.creatBySuccessMessage("密码更新成功");
        }
           return ServerResponse.creatByErrorMessage("密码更新失败");
    }

    /**
     * 更新个人信息
     * @param user
     * @return
     */
    public ServerResponse<User> updateInformation(User user) {
        //用户名不跟新
        //校验新的email是不是已经存在，并且不能是当前用户的email
       int resultCount =  userMapper.checkEmailByUserId(user.getEmail(),user.getId());
       if (resultCount > 0 ) {
           return ServerResponse.creatByErrorMessage("email以存在，请尝试更换email再更新");
       }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
       int updateCount =  userMapper.updateByPrimaryKeySelective(updateUser);
       if (updateCount > 0) {
           return ServerResponse.creatBySuccess("更新个人信息成功",updateUser);
       }
          return ServerResponse.creatByErrorMessage("更新个人信息失败");
    }

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    public ServerResponse<User> getInformation(Integer userId) {
       User user =  userMapper.selectByPrimaryKey(userId);
       if (user == null) {
           return ServerResponse.creatByErrorMessage("找不到当前用户");
       }
       //把密码置空
        user.setPassword(StringUtils.EMPTY);
       return ServerResponse.creatBySuccess(user);
    }


    //backend

    /**
     * 检验是否是管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.creatBySuccess();
        }
          return ServerResponse.creatByError();
    }


}
