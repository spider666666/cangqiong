package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.service.UserService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
}
