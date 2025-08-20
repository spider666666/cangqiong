package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.CommonProperty;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     *定义切入点
     *
     */
    //只要在该包下，并且被表示了注解包中的Auto注解就能被执行到
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    //这里其实就表示了切入点
    public void autoFillPointcut(){}

    //这是一种语法格式，同样还有其他的语法格式,这里是添加语法转换器(在这里使用发射的语法知识点)
    @Before("autoFillPointcut()")
    public void beforeAutoFillPoint(JoinPoint joinPoint){
        //1.定义签名方法(利用签名方法的反射获取到当前注解中的属性值)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);
        CommonProperty commonProperty = annotation.value();


        //2.获取到当前方法中的参数
        Object entity = joinPoint.getArgs()[0];

        if(entity != null) {
            //3.根据不同的方法类型去执行不同的业务逻辑，分别是insert方法和update方法
            //需要更新四个属性
            if(commonProperty == CommonProperty.INSERT){
                //获取到当前方法
                try{
                    Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                    Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
                    Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                    //进行赋值操作
                    setCreateTime.invoke(entity,LocalDateTime.now());
                    setCreateUser.invoke(entity,BaseContext.getCurrentId());
                    setUpdateTime.invoke(entity,LocalDateTime.now());
                    setUpdateUser.invoke(entity,BaseContext.getCurrentId());
                }catch (Exception e){
                    e.printStackTrace();
                }


            }
            //需要更新两个属性
            else if (commonProperty == CommonProperty.UPDATE){
                //获取到当前方法
                try{
                    Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                    Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                    //进行赋值操作
                    setUpdateTime.invoke(entity,LocalDateTime.now());
                    setUpdateUser.invoke(entity,BaseContext.getCurrentId());
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }
    }
}
