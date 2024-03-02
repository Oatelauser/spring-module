package com.abm.module.examples.b;

import com.abm.module.examples.b.controller.TestController;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 概要描述
 * <p>
 * 详细描述
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-04
 * @since 1.0
 */
@AutoConfiguration
@Import({ Config.ImportConfig.class, TestController.class })
public class Config {


    @Bean
    public Alice alice1() {
        return new Alice();
    }

    @Configuration("innerConfigAAAA")
    static class InnerConfig {

        @Bean
        public Alice alice2() {
            return new Alice();
        }
    }

    static class ImportConfig {
        @Bean
        public Alice alice3() {
            return new Alice();
        }
    }

    @Bean
    AAAA aaaa(@Qualifier("innerConfigAAAA") InnerConfig innerConfig) {
        return new AAAA(innerConfig);
    }

    record AAAA(InnerConfig innerConfig) {

    }

}
