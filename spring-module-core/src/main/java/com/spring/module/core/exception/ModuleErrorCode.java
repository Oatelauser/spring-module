package com.spring.module.core.exception;

/**
 * 概要描述
 * <p>
 * 详细描述
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-10
 * @since 1.0
 */
public interface ModuleErrorCode {

    /**
     * 其他引用的模块存在相同的Bean名称
     */
    String MODULE_BEAN_NAME_EXIST = "MB1001";

    /**
     * 模块不存在
     */
    String MODULE_NOT_EXIST = "MR1001";

    /**
     * 模块已经注册
     */
    String MODULE_ALREADY_REGISTRATION = "MR1002";

    /**
     * 当前模块存在父模块，无法注销
     */
    String MODULE_EXIST_PARENTS = "MR1003";

}
