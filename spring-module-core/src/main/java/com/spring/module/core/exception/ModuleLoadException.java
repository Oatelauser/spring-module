package com.spring.module.core.exception;

/**
 * 概要描述
 * <p>
 * 详细描述
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-16
 * @since 1.0
 */
public class ModuleLoadException extends ModuleException {

    public ModuleLoadException(String code, String message) {
        super(code, message);
    }

    public ModuleLoadException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static void invalidModuleFile(String message) throws ModuleLoadException {
        throw new ModuleLoadException("", message);
    }

}
