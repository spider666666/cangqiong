package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

public interface ShoppingCartService {

    void addCart(ShoppingCartDTO shoppingCartDTO);

    void sub(ShoppingCartDTO shoppingCartDTO);
}
