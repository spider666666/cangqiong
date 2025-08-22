package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @Insert("insert into dish (name, category_id, price, image, description) values (#{name},#{categoryId},#{price},#{image},#{description})")
    @Options(useGeneratedKeys = true ,keyProperty = "id")
    void insert(Dish dish);

    Dish queryById(Long id);

    Page<DishVO> queryByPage(DishPageQueryDTO dishPageQueryDTO);

    @Select("select * from dish")
    List<Dish> queryList();

    void deleteBatch(List<Long> ids);

    void update(Dish dish);
}
