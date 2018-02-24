package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 前台用户搜索产品控制器
 * @author fan
 * @date 2018/1/30 10:50
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;


    /**
     * 前台客户端查询产品，如果是下架的产品是不应该显示出来的
     * @param productId
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        return iProductService.detail(productId);
    }

    @RequestMapping(value = "/{productId}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detailRESTful(@PathVariable Integer productId) {
        return iProductService.detail(productId);
    }

    /**
     *客户端根据关键字和排序规则进行分页查询
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword",required = false) String keyword,
                                         @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value = "pageNum",defaultValue = "1")Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                                         @RequestParam(value = "orderBy",defaultValue = "")String orderBy) {
            return iProductService.list(keyword,categoryId,pageNum,pageSize,orderBy);
    }

    @RequestMapping(value = "/{categoryId}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> listRESTfulBadCase(@PathVariable(value = "categoryId")Integer categoryId,
                                         @PathVariable(value = "pageNum")Integer pageNum,
                                         @PathVariable(value = "pageSize")Integer pageSize,
                                         @PathVariable(value = "orderBy")String orderBy) {
        if(pageNum == null) {
            pageNum = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }
        return iProductService.list("",categoryId,pageNum,pageSize,orderBy);
    }

    @RequestMapping(value = "/{keyword}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> listRESTful(@PathVariable(value = "keyword")String keyword,
                                                       @PathVariable(value = "pageNum")Integer pageNum,
                                                       @PathVariable(value = "pageSize")Integer pageSize,
                                                       @PathVariable(value = "orderBy")String orderBy) {
        if(pageNum == null) {
            pageNum = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }
        return iProductService.list(keyword,null,pageNum,pageSize,orderBy);
    }


    //正确的RESTful风格   http://www.happymmall.com/product/category/100012/1/10/price_asc
    @RequestMapping(value = "/category/{categoryId}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> listRESTful(@PathVariable(value = "categoryId")Integer categoryId,
                                                       @PathVariable(value = "pageNum")Integer pageNum,
                                                       @PathVariable(value = "pageSize")Integer pageSize,
                                                       @PathVariable(value = "orderBy")String orderBy) {
        if(pageNum == null) {
            pageNum = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }
        return iProductService.list("",categoryId,pageNum,pageSize,orderBy);
    }

    //http://www.happymmall.com/product/keyword/手机/1/10/price_asc
    @RequestMapping(value = "/keyword/{keyword}/{pageNum}/{pageSize}/{orderBy}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> listRESTfulBadCase(@PathVariable(value = "keyword")String keyword,
                                                       @PathVariable(value = "pageNum")Integer pageNum,
                                                       @PathVariable(value = "pageSize")Integer pageSize,
                                                       @PathVariable(value = "orderBy")String orderBy) {
        if(pageNum == null) {
            pageNum = 1;
        }
        if(pageSize == null) {
            pageSize = 10;
        }
        if(StringUtils.isBlank(orderBy)) {
            orderBy = "price_asc";
        }
        return iProductService.list(keyword,null,pageNum,pageSize,orderBy);
    }




}
