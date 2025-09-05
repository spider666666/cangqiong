package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@Api("店铺操作")
@RequestMapping("/user/shop")
public class ShopController {
    @Autowired
    private ShopService shopService;
    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result getStatus(){
        return shopService.getStatus();

    }
}
