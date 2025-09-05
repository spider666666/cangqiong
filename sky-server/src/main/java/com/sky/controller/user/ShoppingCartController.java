package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Api("购物车相关接口")
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @ApiOperation("添加购物车")
    @PostMapping("/add")
    public Result addCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.addCart(shoppingCartDTO);
        return Result.success();
    }

    @ApiOperation("查看购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> queryList(){
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.querylist(shoppingCart);
        return Result.success(shoppingCartList);
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clean")
    public Result clean(){
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deletelist(userId);
        return Result.success();
    }

    @ApiOperation("减少购买商品数量")
    @PostMapping("/sub")
    public Result clean(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }

}
