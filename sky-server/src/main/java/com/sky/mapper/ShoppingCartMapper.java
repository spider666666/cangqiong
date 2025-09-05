package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    List<ShoppingCart> querylist(ShoppingCart shoppingCart);

    //修改购物车中商品的数量（通常情况下购物车仅仅会修改数量）
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumber(ShoppingCart shoppingCart1);

    @Insert("insert into shopping_cart(name,user_id,dish_id,setmeal_id,dish_flavor,number,amount,image,create_time)" +
            " values(#{name},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{image},#{createTime}) ")
    void insert(ShoppingCart shoppingCart);

    @Select("select * from shopping_cart")
    List<ShoppingCart> queryall();

    void deletelist(Long userId);

    void delete(ShoppingCart shoppingCart);
}
