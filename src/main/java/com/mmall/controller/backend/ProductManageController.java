package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisSharedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author fan
 * @date 2018/1/28 15:32
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * 保存或更新产品
     *
     * @param product
     * @return
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpServletRequest request, Product product) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充业务
            return iProductService.saveOrUpdateProduct(product);
        }
        return ServerResponse.creatByErrorMessage("无权限操作");
    }

    /**
     * 改变产品上下架的状态
     *
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse<String> setSaleStatus(HttpServletRequest request, Integer productId, Integer status) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充业务
            return iProductService.setSaleStatus(productId, status);
        }
        return ServerResponse.creatByErrorMessage("无权限操作");
    }

    /**
     * 获取产品信息
     *
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpServletRequest request, Integer productId) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充业务
            return iProductService.manageProductDetail(productId);
        }
        return ServerResponse.creatByErrorMessage("无权限操作");
    }


    /**
     * 分页查询产品的list
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充业务
            return iProductService.getProductList(pageNum, pageSize);
        }
        return ServerResponse.creatByErrorMessage("无权限操作");
    }

    /**
     * 根据产品名称和ID分页查询产品信息
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpServletRequest request,String productName,Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充业务
            return iProductService.productSearch(productName,productId,pageNum,pageSize);
        }
        return ServerResponse.creatByErrorMessage("无权限操作");
    }

    /**
     * springmvc文件上传
     *upload_file 一定要对应form表单中的name的值
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request) {
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.creatBySuccessMessage("用户未登录，无法获取用户信息");
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充业务
            //upload文件夹会自动在tomcat的root目录下生成
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;

            Map fileMap = Maps.newHashMap();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);
            return ServerResponse.creatBySuccess(fileMap);
        }
        return ServerResponse.creatByErrorMessage("无权限操作");
    }

    @RequestMapping("rich_text.do")
    @ResponseBody
    public Map richTestFileUpload(@RequestParam(value = "upload_file", required = false) MultipartFile file,
                                  HttpServletRequest request, HttpServletResponse response) {
        Map resultMap = Maps.newHashMap();
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            resultMap.put("success", false);
            resultMap.put("message", "用户未登录");
            return resultMap;
        }
        String userJsonStr = RedisSharedPoolUtil.get(loginToken);

        User user = JsonUtil.String2Obj(userJsonStr,User.class);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("message", "用户未登录");
            return resultMap;
        }
        //富文本上传有自己的返回格式比如simditor的要求返回
        /*  "success":true/false,
          "message":具体的信息，
          "file_path":上传后的文件路径*/
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //填充业务
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            if (StringUtils.isBlank(targetFileName)) {
                resultMap.put("success", false);
                resultMap.put("message", "上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            resultMap.put("success", true);
            resultMap.put("message", "上传成功");
            resultMap.put("file_path", url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        } else {
            resultMap.put("success", false);
            resultMap.put("message", "需要权限");
            return resultMap;
        }
    }

}