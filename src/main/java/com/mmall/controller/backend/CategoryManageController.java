package com.mmall.controller.backend;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisSharedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

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
     * @param categoryName
     * @param  parentId 如果不写parentId默认就是O，表示跟节点
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpServletRequest request, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
       /* String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
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
        }*/

        //全部通过拦截器验证是否登录和权限
        return iCategoryService.addCategory(categoryName,parentId);
    }

    /**
     * 更新品类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpServletRequest request,Integer categoryId,String categoryName) {
       /* String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
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
*/
        //全部通过拦截器验证是否登录和权限
        return iCategoryService.updateCategoryName(categoryId,categoryName);
    }

    /**
     * 查找平级子节点的category信息，不递归
     * @param parentId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParalleCategory(HttpServletRequest request,@RequestParam(value = "parentId",defaultValue = "0") Integer parentId) {
       /* String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //判断是不是管理员
        if(iUserService.checkAdminRole(user).isSuccess()) {
            //查询子节点的信息，保持平级
          return iCategoryService.getChildrenParalleCategory(parentId);
        }else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }*/

        //全部通过拦截器验证是否登录和权限
        return iCategoryService.getChildrenParalleCategory(parentId);
    }

    /**
     * 查询当前节点的id和递归子节点的id
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpServletRequest request,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId) {
       /* String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //判断是不是管理员
        if(iUserService.checkAdminRole(user).isSuccess()) {
            //查询当前节点的id和递归子节点的id
           return iCategoryService.selectCategoryAndChildrenById(categoryId);
        }else {
            return ServerResponse.creatByErrorMessage("无权限操作，需要管理员权限");
        }*/

        //全部通过拦截器验证是否登录和权限
        return iCategoryService.selectCategoryAndChildrenById(categoryId);
    }






}
