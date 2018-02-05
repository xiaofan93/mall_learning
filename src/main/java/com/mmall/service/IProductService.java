package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

/**
 * @author fan
 * @date 2018/1/28 15:33
 */
public interface IProductService {

    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId,Integer status);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);

    ServerResponse<PageInfo> productSearch(String productName,Integer productId,Integer pageNum,Integer pageSize);

    ServerResponse<ProductDetailVo> detail(Integer productId);

    ServerResponse<PageInfo> list(String keyword,Integer categoryId,Integer pageNum,Integer pageSize,String orderby);
}
