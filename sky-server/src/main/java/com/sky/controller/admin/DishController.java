package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api("菜品相关接口")
public class DishController {

    /**
     *
     * @param dto
     * @return
     */

    @Autowired
    private DishService dishService;

    @PostMapping
    @ApiOperation("添加菜品")
    public Result addDish(@RequestBody DishDTO dishDto){

        dishService.addDish(dishDto);
        return Result.success();

    }

    /**
     *
     *
     *
     *
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> queryById(@PathVariable("id") Long id){

        DishVO dishVoList= dishService.queryById(id);
        if (dishVoList == null){
            return Result.error("不存在该菜品");
        }
        return Result.success(dishVoList);

    }

    /**
     * 实现菜品的分页查询
     *
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> queryByPage(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.queryByPage(dishPageQueryDTO);
        return Result.success(pageResult);

    }


    /**
     * 实现菜品的起售与停售
     *
     *
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品的起售与停售")
    public Result startOrStop(@PathVariable Integer status , Long id){
        dishService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 查询所有菜品
     *
     *
     *
     *
     */
    @GetMapping("/list")
    @ApiOperation("查询所有的菜品")
    public Result<List<Dish>> queryList(){
        List<Dish> dishList = dishService.queryList();
        return Result.success(dishList);

    }

    /**
     *
     * 实现删除菜品的功能
     *
     *
     */
    @DeleteMapping
    @ApiOperation("(批量)删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        dishService.delete(ids);
        return Result.success();
    }

    /**
     *
     * 实现菜品的修改
     *
     *
     */
    @PutMapping
    @ApiOperation("菜品的修改")
    public Result update(@RequestBody DishDTO dishDTO){
        dishService.update(dishDTO);
        return Result.success();
    }
}