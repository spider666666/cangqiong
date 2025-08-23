package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import com.sky.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImp implements ShopService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result getStatus() {
        //1.获取当前状态
        String s = stringRedisTemplate.opsForValue().get("SHOP_STATUS");
        //2.将得到的数据进行反序列化操作
        if (s == null ){
            return Result.error("当前不存在状态");
        }
        return Result.success(Integer.valueOf(s));


    }

    @Override
    public Result updateStatus(Integer status) {
        stringRedisTemplate.opsForValue().set("SHOP_STATUS",status.toString());
        return Result.success("已经成功修改了店铺状态");
    }
}
