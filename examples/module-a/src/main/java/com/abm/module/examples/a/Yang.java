package com.abm.module.examples.a;

import org.springframework.beans.factory.annotation.Value;

/**
 * 概要描述
 * <p>
 * 详细描述
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-05
 * @since 1.0
 */
public class Yang {

    @Value("${name}")
    private String name;

    @Value("${app}")
    private String app;

    @Override
    public String toString() {
        return "name:" + name + "; app:" + app;
    }
}
