package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Api("店铺操作")
@RequestMapping("/admin/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;
    @ApiOperation("获取营业状态")
    @GetMapping("/status")
    public Result getStatus(){
        return shopService.getStatus();

    }

    @PutMapping("/{status}")
    public Result updateStatus(@PathVariable("status") Integer status){
        return shopService.updateStatus(status);
    }

}
