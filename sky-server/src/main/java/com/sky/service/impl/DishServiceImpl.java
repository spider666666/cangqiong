package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Override
    @Transactional
    public void addDish(DishDTO dishDto) {

        //1.添加菜品信息到数据库中
        //1.1进行拷贝
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDto,dish);
        //1.2添加数据库中
        dishMapper.insert(dish);

        //2.将菜品口味放入数据库中
        //2.1.获取菜品的id
        Long id = dish.getId();
        //获取菜品的口味集合
        List<DishFlavor> flavors = dishDto.getFlavors();
        //为集合中的每个样本添加id属性
        List<DishFlavor> collects = flavors.stream()
                .map(f -> { f.setDishId(id);return f;})
                .collect(Collectors.toList());
        //2，2根据id批量上传菜品口味
        dishFlavorMapper.insert(collects);


        
    }
}
