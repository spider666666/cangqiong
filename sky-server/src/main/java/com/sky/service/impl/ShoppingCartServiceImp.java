package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImp implements ShoppingCartService{


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void addCart(ShoppingCartDTO shoppingCartDTO) {

        ShoppingCart shoppingCart = new ShoppingCart();
        //0.进行属性拷贝
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
//        Long userId = BaseContext.getCurrentId();
        Long userId = 666L;
        shoppingCart.setUserId(userId);

        //1.首先查询数据库中有没有数据
        List<ShoppingCart> shoppingCartlist = shoppingCartMapper.querylist(shoppingCart);

        //2.如果数据库中有这个数据，那么直接加1(其实就一条数据)
        if(shoppingCartlist != null && !shoppingCartlist.isEmpty()){
            ShoppingCart shoppingCart1 = shoppingCartlist.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);
            //更新数据
            shoppingCartMapper.updateNumber(shoppingCart1);
        }
        else{
            //3.如果不存在，直接添加在数据库中
            //根据id查询图片和名称
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if(dishId!= null){
                //查询菜品表
                Dish dish = dishMapper.queryById(dishId);
                shoppingCart.setName(dish.getName());
//                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(LocalDateTime.now());

            }
            else{
                //查询套餐表
                Setmeal setmeal = setmealMapper.queryById(setmealId);
//                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setNumber(1);
                shoppingCart.setCreateTime(LocalDateTime.now());

            }

            //将查询到的对象存入数据库
            shoppingCartMapper.insert(shoppingCart);

        }



    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        //0.进行属性拷贝
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
//        Long userId = BaseContext.getCurrentId();
        Long userId = 666L;
        shoppingCart.setUserId(userId);

        //1.首先查询数据库中有没有数据
        List<ShoppingCart> shoppingCartlist = shoppingCartMapper.querylist(shoppingCart);

        //2.如果数据库中有这个数据，那么直接加1(其实就一条数据)
        if(shoppingCartlist != null && !shoppingCartlist.isEmpty()){
            ShoppingCart shoppingCart1 = shoppingCartlist.get(0);
            if (shoppingCart1.getNumber() == 1){
                shoppingCartMapper.delete(shoppingCart);
            }
            //否则直接更新数据减一
            shoppingCart1.setNumber(shoppingCart1.getNumber() - 1);
            //更新数据
            shoppingCartMapper.updateNumber(shoppingCart1);
        }

    }
}
