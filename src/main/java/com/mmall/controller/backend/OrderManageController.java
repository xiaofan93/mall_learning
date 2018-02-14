package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author fan
 * @date 2018/2/5 14:25
 */
@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;

    /**
     * 分页查询订单页
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
   public ServerResponse<PageInfo> orderList(HttpServletRequest request, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                             @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
       if (user == null) {
           return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
       }
       //判断是不是管理员
       if(iUserService.checkAdminRole(user).isSuccess()) {
           //添加业务逻辑
           return iOrderService.manageList(pageNum,pageSize);
       }else {
           return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
       }
   }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse orderDetail(HttpServletRequest request,Long orderNo) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //判断是不是管理员
        if(iUserService.checkAdminRole(user).isSuccess()) {
            //添加业务逻辑
            return iOrderService.manageDetail(orderNo);
        }else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 搜索订单
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderSearch(HttpServletRequest request,Long orderNo, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //判断是不是管理员
        if(iUserService.checkAdminRole(user).isSuccess()) {
            //添加业务逻辑
            return iOrderService.manageSearch(orderNo,pageNum,pageSize);
        }else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 管理员发货
     * @param orderNo
     * @return
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse orderSendGoods(HttpServletRequest request,Long orderNo) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //判断是不是管理员
        if(iUserService.checkAdminRole(user).isSuccess()) {
            //添加业务逻辑
            return iOrderService.manageSendGoods(orderNo);
        }else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }
    }



}
