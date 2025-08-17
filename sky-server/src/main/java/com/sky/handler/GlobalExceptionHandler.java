package com.sky.handler;

import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result employeeExceptionHandler(SQLIntegrityConstraintViolationException ex){
        //Duplicate entry 'jklfds' for key 'employee.idx_username' 可能会报错的例子
        String message = ex.getMessage();
        if(message.contains("Duplicate entry")){
            //重复的键值对
            String name = message.split(" ")[3];
            return Result.error("用户" + name + "已存在");

        }
        else{
            //TODO 可能以后还会添加其他的错误类型
            return Result.error("未知错误");
        }

    }

}
