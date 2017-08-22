package com.meituan.robust.patch.annotaion;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mivanzhang on 16/12/19.
 * 用来标记新增的类和方法
 * annotaion used for add classes or methods,classes and methods will be packed into patch.jar/patch.apk
 */

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface Add {
    String value() default "";
}