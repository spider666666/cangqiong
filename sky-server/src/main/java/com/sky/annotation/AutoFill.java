package com.sky.annotation;


import com.sky.enumeration.CommonProperty;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.aspectj.lang.annotation.Before;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//target表示注解添加的目标
//Retention是保留的意思，保证了该注解会生效
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //定义为注解本身的属性
    CommonProperty value();

    //使用前置通知


}

