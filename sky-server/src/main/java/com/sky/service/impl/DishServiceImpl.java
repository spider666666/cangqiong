package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {


    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SetmealMapper setmealMapper;

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

    @Override
    public DishVO queryById(Long id) {
        Dish dish = dishMapper.queryById(id);

        //将查询的结果进行转化
        List<DishFlavor> dishFlavorsList = dishFlavorMapper.queryById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        dishVO.setFlavors(dishFlavorsList);
        return dishVO;
    }

    @Override
    public PageResult queryByPage(DishPageQueryDTO dishPageQueryDTO) {
        //1.实现分页
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        //2.进行查询
        Page<DishVO> pages = dishMapper.queryByPage(dishPageQueryDTO);

        //3.返回
        return new PageResult(pages.getTotal(),pages.getResult());
    }

    @Override
    public void startOrStop(Integer status,Long id) {
        //1.新建数据对象
        Dish dish = dishMapper.queryById(id);
        dish.setStatus(status);
        //2.对数据进行修改
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> queryList() {
        List<Dish> dishList = dishMapper.queryList();
        return dishList;
    }

    @Override
    @Transactional
    //这里就是批量删除的逻辑，要删除就一起删除的意思
    public void delete(List<Long> ids) {
        //1.判断当前是否是起售状态，否则不让删除(循环遍历，无法批量进行操作)
        for (Long id:ids){
            Dish dish = dishMapper.queryById(id);
//            如果不是停售状态
            if (Objects.equals(dish.getStatus(), StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }

        }

        //2.判断当前菜品套餐中是否含有它，如果包含，则不能删除(注意，这里是多对多的关系)
        Long count = setmealMapper.countByDishIds(ids);
        if (count > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);

        }

        //3.如果全都满足，删除菜品中的数据
        dishMapper.deleteBatch(ids);

        //4.删除相关联的口味数据
        dishFlavorMapper.deleteBatch(ids);
    }

    @Override
    @Transactional
    public void update(DishDTO dishDto) {
        //更新菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDto,dish);
        dishMapper.update(dish);
        //更新口味

        //2.将菜品口味放入数据库中
        //2.1获取菜品的口味集合
        List<DishFlavor> flavors = dishFlavorMapper.queryById(dish.getId());
        if(flavors != null && !flavors.isEmpty()){
            //2.2首先删除口味
            dishFlavorMapper.delete(dish.getId());
        }
        //2.3.获取菜品的id
        Long id = dish.getId();
        //为集合中的每个样本添加id属性
        List<DishFlavor> flavors1 = dishDto.getFlavors();
        List<DishFlavor> collects = flavors1.stream()
                .map(f -> { f.setDishId(id);return f;})
                .collect(Collectors.toList());
        //2.4然后添加口味
        dishFlavorMapper.insert(collects);

    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        //根据分类id返回菜品列表
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.queryById(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }


}
