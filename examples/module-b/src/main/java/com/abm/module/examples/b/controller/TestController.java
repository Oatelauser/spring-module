package com.abm.module.examples.b.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 概要描述
 * <p>
 * 详细描述
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-12
 * @since 1.0
 */
@RestController
public class TestController {

    @GetMapping("/ttt")
    public String test() {
        return "ttt";
    }
}
