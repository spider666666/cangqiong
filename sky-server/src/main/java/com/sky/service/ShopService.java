package com.sky.service;

import com.sky.result.Result;

public interface ShopService {


    Result getStatus();

    Result updateStatus(Integer status);
}
