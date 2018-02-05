package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author fan
 * @date 2018/2/1 19:33
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        //insert语句需要设置成主键自增，在SQL语句里添加两个属性
       int rowCount = shippingMapper.insert(shipping);
       if (rowCount > 0) {
           Map result = Maps.newHashMap();
           //需要把新加的地址Id返回给前端
           result.put("ShippingId",shipping.getId());
           return ServerResponse.creatBySuccess("新建地址成功",result);
       }
       return ServerResponse.creatByErrorMessage("新建地址失败");
    }

    //涉及到横向越权的问题，需要重写SQL语句，把userId关联进去
    public ServerResponse<String> del(Integer userId,Integer shippingId) {
       int rowCount = shippingMapper.deleteByShippingIdUserId(shippingId,userId);
       if (rowCount > 0) {
           return ServerResponse.creatBySuccessMessage("删除地址成功");
       }
       return ServerResponse.creatByErrorMessage("删除地址失败");
    }

    /**
     * 传需要更新的id,不需要userID
     * @param userId
     * @param shipping
     * @return
     */
    public ServerResponse update(Integer userId,Shipping shipping) {
        shipping.setUserId(userId);
        //用户Id是不能更新的，在sql语句中要删除,并且userId要作为更新条件加在最后，防止横向越权
       int rowCount = shippingMapper.updateByShipping(shipping);
       if (rowCount > 0) {
           return ServerResponse.creatBySuccessMessage("更新地址成功");
       }
       return ServerResponse.creatByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId,Integer shippingId) {
        Shipping shipping = shippingMapper.selectByShippingIdUserId(shippingId,userId);
        if (shipping == null) {
            return ServerResponse.creatByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.creatBySuccess("查询地址成功",shipping);
    }


    public ServerResponse<PageInfo> list(Integer userId,Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
       List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
       PageInfo pageInfo = new PageInfo(shippingList);
       return ServerResponse.creatBySuccess(pageInfo);
    }

}
