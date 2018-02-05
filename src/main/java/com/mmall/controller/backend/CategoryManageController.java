package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 产品分类控制器
 * @author fan
 * @date 2018/1/27 16:55
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 添加品类
     * @param session
     * @param categoryName
     * @param  parentId 如果不写parentId默认就是O，表示跟节点
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session,String categoryName,@RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //判断是不是管理员
        if(iUserService.checkAdminRole(user).isSuccess()) {
            //是管理员
            //添加分类逻辑
           return iCategoryService.addCategory(categoryName,parentId);
        }else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 更新品类名称
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session,Integer categoryId,String categoryName) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //判断是不是管理员
        if(iUserService.checkAdminRole(user).isSuccess()) {
            //更新操作
           return iCategoryService.updateCategoryName(categoryId,categoryName);
        }else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 查找平级子节点的category信息，不递归
     * @param session
     * @param parentId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParalleCategory(HttpSession session,@RequestParam(value = "parentId",defaultValue = "0") Integer parentId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //判断是不是管理员
        if(iUserService.checkAdminRole(user).isSuccess()) {
            //查询子节点的信息，保持平级
          return iCategoryService.getChildrenParalleCategory(parentId);
        }else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 查询当前节点的id和递归子节点的id
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId) {
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //判断是不是管理员
        if(iUserService.checkAdminRole(user).isSuccess()) {
            //查询当前节点的id和递归子节点的id
           return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }
    }






}
