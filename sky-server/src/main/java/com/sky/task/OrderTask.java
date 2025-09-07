package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

//    @Scheduled(cron = "0 * * * * *")
    public void processOrder(){
        log.info("定时处理未支付的订单 {}", LocalDateTime.now());

        //1.判断预计超时的时间
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        //2.查询在未支付的状态下订单超时的订单
        List<Orders> orderslist = orderMapper.queryOutTimeOrderStatus(Orders.PENDING_PAYMENT, time);

        //如果订单列表为空，那么无所谓，直接返回结果,如果订单列表不为空，需要把订单进行取消
        if (orderslist != null && !orderslist.isEmpty()){
            //对于列表的更新方法，使用遍历是一种不错的选择
            for (Orders order:orderslist){
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时,未在15分钟内进行支付");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }

        }

    }
    //处理派送状态未取消的问题
//    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliver(){

        //1.判断预计派送超时的时间
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        //2.查询在未支付的状态下订单超时的订单
        List<Orders> orderslist = orderMapper.queryOutTimeOrderStatus(Orders.DELIVERY_IN_PROGRESS, time);
        //如果订单列表为空，那么无所谓，直接返回结果,如果订单列表不为空，需要把订单进行取消
        if (orderslist != null && !orderslist.isEmpty()){
            //对于列表的更新方法，使用遍历是一种不错的选择
            for (Orders order:orderslist){
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }

        }


    }
}
