package com.abm.module.examples.a;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 概要描述
 * <p>
 * 详细描述
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-05
 * @since 1.0
 */
@AutoConfiguration
public class Config {

    @Bean
    public Yang yang() {
        return new Yang();
    }

    @Bean
    public User user1(Yang yang) {
        return new User(yang);
    }



}
