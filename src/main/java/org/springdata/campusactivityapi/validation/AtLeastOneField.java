package org.springdata.campusactivityapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类级别校验：fields 里指定的那些字段，至少要有一个不为空。
 * 字符串会额外判空串和纯空格。
 *
 * 对比你写过的 @AutoFill：
 *   相同的 —— @Target 声明贴在哪、@Retention(RUNTIME) 声明运行时可读
 *   新增的 —— @Constraint，它指定"谁来处理这个注解"
 *
 * 注意：只要加了 @Constraint，下面这三个方法就【必须】声明，少一个启动就报错，
 * 这是 Bean Validation 的硬性规定，不是可选的。
 */
@Documented
@Target(ElementType.TYPE)          // TYPE = 贴在类上；你 AutoFill 用 METHOD = 贴在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时保留，这样框架才能用反射读到它
@Constraint(validatedBy = AtLeastOneFieldValidator.class)  // ← 指定处理器
public @interface AtLeastOneField {

    /** 校验失败时给前端的提示，使用方可以覆盖 */
    String message() default "至少填写一个字段";

    /** 要检查的字段名，比如 {"nickName", "status"} */
    String[] fields();

    /** 分组校验用，暂时用不到，但必须声明 */
    Class<?>[] groups() default {};

    /** 携带额外元数据用，暂时用不到，但必须声明 */
    Class<? extends Payload>[] payload() default {};
}
