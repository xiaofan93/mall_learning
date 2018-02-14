package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author fan
 * @date 2018/2/3 14:23
 */
@Service("iOrderService")
@Slf4j
public class OrderServiceImpl implements IOrderService {


    private static AlipayTradeService   tradeService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;


    public ServerResponse createOrder(Integer userId,Integer shippingId) {

        //从购物车中获取已经勾选的数据，也就是即将生成订单的
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        //得到购物车的订单明细
        ServerResponse serverResponse = this.getCartOrderItem(userId,cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        //计算这个订单的总价
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);
        //生成订单
       Order order = this.assembleOrder(userId,shippingId,payment);
       if (order == null) {
           return ServerResponse.creatByErrorMessage("生成订单失败");
       }
       if (CollectionUtils.isEmpty(orderItemList)) {
           return ServerResponse.creatByErrorMessage("购物车为空");
       }
       //为订单明细表添加定单号
       for (OrderItem orderItem : orderItemList) {
           orderItem.setOrderNo(order.getOrderNo());
       }
       //mybatis批量插入订单明细表
        orderItemMapper.batchInsert(orderItemList);

       //订单生成成功，我们要减少我们的库存
        this.reduceProductStock(orderItemList);
        //清空购物车
        this.cleanCart(cartList);
        //返回给前端我们封装的数据
        OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
        return ServerResponse.creatBySuccess(orderVo);
    }

    //封装OrderVo
    private OrderVo assembleOrderVo(Order order,List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getMsg());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getMsg());

        orderVo.setShippingId(order.getShippingId());
        //设置收货地址 一个订单对应一个收货地址
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            //组装ShippingVo
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getCreateTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        //设置OrderItemVo
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }


    //组装OrderItemVo
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
       OrderItemVo orderItemVo = new OrderItemVo();
       orderItemVo.setOrderNo(orderItem.getOrderNo());
       orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
       orderItemVo.setProductId(orderItem.getProductId());
       orderItemVo.setProductImage(orderItem.getProductImage());
       orderItemVo.setProductName(orderItem.getProductName());
       orderItemVo.setQuantity(orderItem.getQuantity());
       orderItemVo.setTotalPrice(orderItem.getTotalPrice());

       orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
       return orderItemVo;
    }

    //组装ShippingVo
    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }


    //清空购物车
    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    //减少产品的库存
    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
           Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
           product.setStock(product.getStock()-orderItem.getQuantity());
           productMapper.updateByPrimaryKeySelective(product);
        }
    }

    //生成订单
    private Order assembleOrder(Integer userId,Integer shippingId,BigDecimal payment) {
        Order order = new Order();
        long orderNo = this.generateOrderNo();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPayment(payment);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        //运费
        order.setPostage(0);
        //付款方式
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        //收货地址
        order.setShippingId(shippingId);
        //付款时间
        //发货时间
       int rowCount = orderMapper.insert(order);
       if (rowCount > 0) {
           return order;
       }
       return null;
    }

    //生成订单号
    private long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        long orderNo = currentTime + new Random().nextInt(100);
        return orderNo;
    }

    //计算订单总价
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
           payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }


    //获取购物车中的产品明细
    private ServerResponse getCartOrderItem(Integer userId,List<Cart> cartList) {

        List<OrderItem> orderItemList = Lists.newArrayList();

        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.creatByErrorMessage("购物车为空");
        }
        //校验购物车的数据，包括产品的状态和数量
        for (Cart cartItem : cartList) {
            //获取购物车中的产品信息
           Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
                return ServerResponse.creatByErrorMessage("产品"+product.getName()+"不是在线售卖状态");
            }
            //校验库村
            if (cartItem.getQuantity() > product.getStock()) {
                return ServerResponse.creatByErrorMessage("产品"+product.getName()+"库存不足");
            }
            OrderItem orderItem = new OrderItem();
            //设置订单明细表
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.creatBySuccess(orderItemList);
    }

    //取消订单
    public ServerResponse<String> cancelOrder(Integer userId,Long orderNo) {
       Order order = orderMapper.selectByOrderNoAndUserId(orderNo,userId);
       if (order == null) {
           return ServerResponse.creatByErrorMessage("该用户没有此订单");
       }
       if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
           return ServerResponse.creatByErrorMessage("已付款，无法取消订单");
       }
       Order updateOrder = new Order();
       updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
       updateOrder.setId(order.getId());
      int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
      if (rowCount > 0) {
          return ServerResponse.creatBySuccess();
      }
      return ServerResponse.creatByError();
    }

    /**
     *  查看购物车已选商品的信息
     * @param userId
     * @return
     */
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        //从购物车中获取数据
       List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
       ServerResponse serverResponse = this.getCartOrderItem(userId,cartList);
       if (!serverResponse.isSuccess()){
           return serverResponse;
       }
      List<OrderItem> orderItemList =(List<OrderItem>) serverResponse.getData();

      List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
      for (OrderItem orderItem : orderItemList) {
         OrderItemVo orderItemVo= this.assembleOrderItemVo(orderItem);
          orderItemVoList.add(orderItemVo);
          payment = BigDecimalUtil.add(payment.doubleValue(),orderItem.getTotalPrice().doubleValue());
      }
      orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
      orderProductVo.setProductTotalPrice(payment);
      orderProductVo.setOrderItemVoList(orderItemVoList);
      return ServerResponse.creatBySuccess(orderProductVo);
    }

    /**
     * 查询订单的详细信息
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse<OrderVo> getOrderDetail(Integer userId,Long orderNo) {
       Order order = orderMapper.selectByOrderNoAndUserId(orderNo,userId);
       if (order != null) {
          List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoUserId(order.getOrderNo(),userId);
          OrderVo orderVo = assembleOrderVo(order,orderItemList);
          return ServerResponse.creatBySuccess(orderVo);
       }
       return ServerResponse.creatByErrorMessage("没有找到该订单");
    }

    /**
     * 分页查询所有订单的信息
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getOrderList(Integer userId,Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
       List<Order> orderList = orderMapper.selectByUserId(userId);
       //组装List<OrderVo>
       List<OrderVo> orderVoList = assembleOrderVoList(orderList,userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.creatBySuccess(pageInfo);
    }

    //组装orderVoList
    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null) {
                //todo 管理员不需要userID
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            }else {
                orderItemList = orderItemMapper.selectByOrderNoUserId(order.getOrderNo(),userId);
            }
           OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }






    //backend

    /**
     * 后台分页查询订单页
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> manageList(Integer pageNum,Integer pageSize) {
          PageHelper.startPage(pageNum,pageSize);
          List<Order> orderList = orderMapper.selectAllOrder();
         List<OrderVo> orderVoList = this.assembleOrderVoList(orderList,null);
         PageInfo resultPageInfo = new PageInfo(orderList);
        resultPageInfo.setList(orderVoList);
        return ServerResponse.creatBySuccess(resultPageInfo);
    }

    /**
     * 后台查询订单
     * @param orderNo
     * @return
     */
    public ServerResponse<OrderVo> manageDetail(Long orderNo) {
       Order order = orderMapper.selectByOrderNo(orderNo);
       if (order != null) {
          List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
         OrderVo orderVo = assembleOrderVo(order,orderItemList);
         return ServerResponse.creatBySuccess(orderVo);
       }
       return ServerResponse.creatByErrorMessage("没有该订单");
    }

    /**
     * 按订单号搜索，以后会进行模糊搜索，所有要分页
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> manageSearch(Long orderNo,Integer pageNum,Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
          List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
          OrderVo orderVo = assembleOrderVo(order,orderItemList);

          PageInfo resultPageInfo = new PageInfo(Lists.newArrayList(order));
            resultPageInfo.setList(Lists.newArrayList(orderVo));
            return ServerResponse.creatBySuccess(resultPageInfo);
        }
        return ServerResponse.creatByErrorMessage("没有该订单");
    }

    //管理员发货
    public ServerResponse<String> manageSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);

        if (order != null) {
           if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
               order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
               order.setSendTime(new Date());
               orderMapper.updateByPrimaryKeySelective(order);
               return ServerResponse.creatByErrorMessage("发货成功");
           }
        }
        return ServerResponse.creatByErrorMessage("没有该订单");
    }











    /**
     * 支付生成二维码
     * @param orderNo
     * @param userId
     * @param path
     * @return
     */
        public ServerResponse pay(Long orderNo,Integer userId,String path) {
        Map<String,String> resultMap = Maps.newHashMap();
       Order order = orderMapper.selectByOrderNoAndUserId(orderNo,userId);
       if (order == null) {
           return ServerResponse.creatByErrorMessage("用户没有该订单");
       }
       resultMap.put("orderNo",String.valueOf(order.getOrderNo()));


        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("happymall扫码支付，订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品总价为:").append(totalAmount).toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<>();

        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoUserId(orderNo,userId);
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        for (OrderItem orderItem : orderItemList) {
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(),
                    orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);

        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径 二维码文件生成的路径位置
                //注意path后面一定要加个/
                String qrPath = String.format(path+"/qr-%s.png",response.getOutTradeNo());
                //文件名
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                log.info("filePath:" + qrPath);
                //利用文件全路径生成二维码
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                //把生成的二维码图片上传到ftp服务器上
                File targetFile = new File(path,qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("上传二维码异常",e);
                }
                log.info("qrPath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.creatBySuccess(resultMap);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.creatBySuccessMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.creatBySuccessMessage("系统异常，预下单状态未知!!!");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.creatBySuccessMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    /**
     * 支付宝回调验证成功之后的业务
     * @param params
     * @return
     */
    public ServerResponse alipayCallback(Map<String,String> params) {
            //商家订单号
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        //支付宝交易号
        String tradeNo = params.get("trade_no");
        //交易状态
        String tradeStatus = params.get("trade_status");

       Order order = orderMapper.selectByOrderNo(orderNo);
       if (order == null) {
           return ServerResponse.creatByErrorMessage("不是小凡商店的订单，忽略回调");
       }
       if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
           return ServerResponse.creatBySuccessMessage("这是支付宝的重复调用");
       }
       if (Const.AilpayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
           //返回的交易状态是成功的 ,更新订单的信息
           order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));//付款时间
           order.setStatus(Const.OrderStatusEnum.PAID.getCode());
           orderMapper.updateByPrimaryKeySelective(order);
       }

       //生成支付信息表中的信息
        PayInfo payInfo = new PayInfo();
       payInfo.setUserId(order.getUserId());
       payInfo.setOrderNo(orderNo);
       payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode()); //支付平台
        payInfo.setPlatformNumber(tradeNo);//支付宝生成的流水号
        payInfo.setPlatformStatus(tradeStatus);//支付宝支付状态

        payInfoMapper.insert(payInfo);

        return ServerResponse.creatBySuccess();
    }

    /**
     * 查询订单的支付状态
     * @param userId
     * @param orderNo
     * @return
     */
    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo) {
       Order order = orderMapper.selectByOrderNoAndUserId(orderNo, userId);
       if (order == null) {
           return ServerResponse.creatByErrorMessage("用户没有该订单");
       }
       if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
           return ServerResponse.creatBySuccess();
       }
       return ServerResponse.creatByError();
    }



}
