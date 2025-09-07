package com.sky.service.impl;

import cn.hutool.db.sql.Order;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.service.UserService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.webSocket.WebSocketServer;
import io.swagger.util.Json;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.sky.entity.Orders.PENDING_PAYMENT;
import static com.sky.entity.Orders.UN_PAID;

@Service
public class OrderServiceImp implements OrderService {

    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //1.根据传入的信息创建订单表
        //1.1创建订单号
        //订单号的创建一般使用uuid的方法
        UUID uuid = UUID.randomUUID();
        //3.返回相应的vo给前端

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setNumber(uuid.toString());
        //接下来查询前端没有的信息

        orders.setStatus(UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(PENDING_PAYMENT);
        //查找用户名和手机号
        Long userId = BaseContext.getCurrentId();
        User user = userService.queryById(userId);
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        //根据id查询用户地址
        AddressBook addressBook = addressBookMapper.getById(orders.getAddressBookId());
        //处理异常信息
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        orders.setAddress(addressBook.getDetail());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);

        //创建订单明细表,订单明细表中的数据可以在数据库中去查询
        ShoppingCart shoppingCart= ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.querylist(shoppingCart);
        if(shoppingCartList == null || shoppingCartList.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //将shoppingCartList中的用户名字段和创建时间字段剔除，并且添加订单id的字段
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(shop -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shop, orderDetail);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());

        //接下来就是批量插入细节表的操作
        orderDetailMapper.insertBatch(orderDetailList);

        //既然已经创建了订单，这里把购物车进行清空的操作
        shoppingCartMapper.deletelist(userId);
        //接下来是创建订单返回信息
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().orderAmount(ordersSubmitDTO.getAmount())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .build();

        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.queryById(userId);

        //这里先暂且跳过微信支付的步骤
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //模拟，下单业务完成之后进行消息的推送，提醒当前用户下单了(通常使用json格式进行传递数据)
        HashMap<String,Object> map = new HashMap<>();
        map.put("type",1);//1表示用户的下单，2表示用户的催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号"+outTradeNo);
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);

    }

    @Override
    public void remindOrder(Long id) {
        Orders orders = orderMapper.queryById(id);
        if(orders!= null){
            //1.添加响应数据
            HashMap<String,Object> map = new HashMap<>();
            map.put("type",2);
            map.put("orderId",id);
            map.put("content","订单号"+orders.getNumber());
            String jsonString = JSON.toJSONString(map);

            //2.进行催单
            webSocketServer.sendToAllClient(jsonString);
        }

    }

}
