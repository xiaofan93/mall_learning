package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
   public ServerResponse<PageInfo> orderList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                             @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize) {
       User user = (User)session.getAttribute(Const.CURRENT_USER);
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
    public ServerResponse orderDetail(HttpSession session,Long orderNo) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
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
     * @param session
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderSearch(HttpSession session,Long orderNo, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
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
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse orderSendGoods(HttpSession session,Long orderNo) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
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
