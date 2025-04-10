package com.github.flowengine.springboot.autoconfigure;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface FlowTaskDelegate {
    String[] value() default {};
}
