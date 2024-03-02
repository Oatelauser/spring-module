package com.spring.module.core.exception;

import static com.spring.module.core.exception.ModuleErrorCode.*;

/**
 * 模块注册异常
 *
 * @author <a href="mailto:545896770@qq.com">DearYang</a>
 * @date 2024-01-10
 * @since 1.0
 */
public class ModuleRegistryException extends ModuleException {

    public ModuleRegistryException(String code, String message) {
        super(code, message);
    }

    public ModuleRegistryException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public static void alreadyExistModule(String message) throws ModuleRegistryException {
        throw  new ModuleRegistryException(MODULE_ALREADY_REGISTRATION, message);
    }

    public static void existParentsModule(String message) throws ModuleRegistryException  {
        throw new ModuleRegistryException(MODULE_EXIST_PARENTS, message);
    }

    public static void notExistModule(String message) throws ModuleRegistryException  {
        throw new ModuleRegistryException(MODULE_NOT_EXIST, message);
    }

}
