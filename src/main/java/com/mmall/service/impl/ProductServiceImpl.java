package com.mmall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fan
 * @date 2018/1/28 15:33
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 更新或添加产品
     *
     * @param product
     * @return
     */
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            //把产品子图的第一个图片作为主图
            String[] subImageArray = product.getSubImages().split(",");
            if (subImageArray.length > 0) {
                product.setMainImage(subImageArray[0]);
            }

            if (product.getId() != null) {
                int rowCount = productMapper.updateByPrimaryKey(product);
                if (rowCount > 0) {
                    return ServerResponse.creatBySuccessMessage("更新产品成功");
                }
                return ServerResponse.creatByErrorMessage("更新产品失败");
            } else {
                int rowCount = productMapper.insert(product);
                if (rowCount > 0) {
                    return ServerResponse.creatBySuccessMessage("添加产品成功");
                }
                return ServerResponse.creatByErrorMessage("添加产品失败");
            }
        }
        return ServerResponse.creatByErrorMessage("新增或更新产品参数不正确");
    }

    /**
     * 修改产品销售状态
     * @param productId
     * @param status
     * @return
     */
    public ServerResponse<String> setSaleStatus(Integer productId,Integer status) {
        if (productId == null || status ==null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
       int rowCount =  productMapper.updateByPrimaryKeySelective(product);
       if (rowCount > 0) {
           return ServerResponse.creatBySuccessMessage("修改产品销售状态成功");
       }
        return ServerResponse.creatByErrorMessage("修改产品销售状态失败");
    }

    /**
     *后台获取产品详细信息
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product =  productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.creatByErrorMessage("产品以下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.creatBySuccess(productDetailVo);

    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();

        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setId(product.getId());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setName(product.getName());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setSubImages(product.getSubImages());

        //设置上传图片域的前缀
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","ftp.server.http.prefix"));
        //设置父节点ParentCategoryId
       Category category =  categoryMapper.selectByPrimaryKey(product.getCategoryId());
       if (category == null) {
           productDetailVo.setParentCategoryId(0); //默认的根节点
       }else {
           productDetailVo.setParentCategoryId(category.getParentId());
       }
       //把从数据库中取出的时间格式转化成String
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getProductList(int pageNum,int pageSize) {
        //开始分页
        //写之间的逻辑SQL
        //收尾
        PageHelper.startPage(pageNum,pageSize);
       List<Product> productList =  productMapper.selectList();

       List<ProductListVo> productListVoList = new ArrayList<>();
       for (Product productItem : productList) {
          ProductListVo productListVo = assembleProductListVo(productItem);
           productListVoList.add(productListVo);
       }
       //把分页查询的出的结果放入pageResult中
       PageInfo pageResult = new PageInfo(productList);
       pageResult.setList(productListVoList);
       return ServerResponse.creatBySuccess(pageResult);

    }

    //组装ProductListVo
    private ProductListVo assembleProductListVo(Product product) {
         ProductListVo productListVo = new ProductListVo();

         productListVo.setId(product.getId());
         productListVo.setCategoryId(product.getCategoryId());
         productListVo.setMainImage(product.getMainImage());
         productListVo.setPrice(product.getPrice());
         productListVo.setStatus(product.getStatus());
         productListVo.setSubtitle(product.getSubtitle());
         productListVo.setName(product.getName());

         productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","ftp.server.http.prefix"));
         return productListVo;
    }


    /**
     * 后台产品分页搜索查询
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> productSearch(String productName,Integer productId,Integer pageNum,Integer pageSize) {
        //开始分页
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        //转换成ProductListVo对象
        List<ProductListVo> productListVoList = new ArrayList<>();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        //分页的结束
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.creatBySuccess(pageResult);
    }

    /**
     * 前台用户搜索产品
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        if (productId == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product =  productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.creatByErrorMessage("产品以下架或者删除");
        }
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.creatByErrorMessage("产品以下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.creatBySuccess(productDetailVo);
    }

    /**
     * 前端客户按关键字和产品分类id查询使用产品的信息
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    public ServerResponse<PageInfo> list(String keyword,Integer categoryId,Integer pageNum,Integer pageSize,String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.creatByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                //说明没有该分类，还没有关键字，这时候返回一个空的结果集，不报错
                PageHelper.startPage(pageNum,pageSize);
                List<ProductDetailVo> productDetailVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productDetailVoList);
                return ServerResponse.creatBySuccess(pageInfo);
            }
            //递归查询出所有的子节点id
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        //拼接关键字
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum,pageSize);
        //排序处理
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+ " " +orderByArray[1]);
            }
        }
        //三元运算符走判断空的情况
       List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size() == 0?null:categoryIdList);

        //处理分页符结尾
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList) {
           ProductListVo productListVo = assembleProductListVo(productItem);
           productListVoList.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.creatBySuccess(pageInfo);
    }







}
