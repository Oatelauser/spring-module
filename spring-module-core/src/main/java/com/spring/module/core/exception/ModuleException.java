package com.spring.module.core.exception;

import static com.spring.module.core.exception.ModuleErrorCode.MODULE_BEAN_NAME_EXIST;

/**
 * 模块基础异常
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-10
 * @since 1.0
 */
public class ModuleException extends RuntimeException {

    private final String code;

    public ModuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ModuleException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static void alreadyExistBeanName(String message) throws ModuleException {
        throw new ModuleException(MODULE_BEAN_NAME_EXIST, message);
    }

}
