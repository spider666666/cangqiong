package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.result.Result;
import com.sky.vo.UserLoginVO;

public interface UserService {

    Result<UserLoginVO> login(UserLoginDTO userLoginDTO);
}
