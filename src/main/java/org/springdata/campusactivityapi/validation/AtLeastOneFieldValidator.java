package org.springdata.campusactivityapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

/**
 * @AtLeastOneField 的处理器（这就是"注解背后真正干活的那个人"）。
 *
 * 接口的两个泛型参数：
 *   第一个 AtLeastOneField —— 说明这个校验器服务于哪个注解
 *   第二个 Object          —— 被校验的对象类型；写 Object 表示不限定具体类型
 *
 * 生命周期（由框架控制，不用自己调）：
 *   创建实例 → initialize() 跑一次 → 之后每次校验调 isValid()
 */
public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, Object> {

    private String[] fields;

    /**
     * 初始化方法，整个应用生命周期里只执行一次。
     *
     * 作用：把注解上写的 fields = {"nickName", "status"} 读出来存到成员变量，
     * 这样后面每次校验就不用重新解析注解了（省性能）。
     *
     * @param annotation 加在类上的那个 @AtLeastOneField 实例
     */
    @Override
    public void initialize(AtLeastOneField annotation) {
        this.fields = annotation.fields();
    }

    /**
     * 真正的校验逻辑，每次请求都会调。
     *
     * @param value   被校验的对象，这里就是 UpdateUserDTO 的实例
     * @param context 校验上下文，可以用来改写错误信息；这里没用到
     * @return true = 校验通过，false = 校验失败（注意别搞反，返回 true 是"没问题"）
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {

        // 对象本身是 null 时直接放行，交给 @NotNull 之类的注解去管，避免重复报错
        if (value == null) {
            return true;
        }

        // 遍历注解上声明的每一个字段名
        for (String fieldName : fields) {
            try {
                // 反射第一步：按名字拿到这个字段（private 字段也能拿到）
                Field field = value.getClass().getDeclaredField(fieldName);

                // 反射第二步：private 字段默认不允许外部读写，这行是打开权限
                field.setAccessible(true);

                // 反射第三步：读出「当前这个对象」上该字段的值
                Object fieldValue = field.get(value);

                if (fieldValue == null) {
                    continue;   // 这个字段没填，去看下一个字段
                }

                // Java 16+ 的模式匹配：如果是 String 类型，直接转成 str 变量
                if (fieldValue instanceof String str) {
                    // 字符串要特殊处理：空串 "" 和纯空格 "   " 都算「没填」
                    if (!str.isBlank()) {
                        return true;    // 找到有效值，整个校验直接通过
                    }
                } else {
                    // 非字符串（Integer、Long 等）：只要不是 null 就算有值
                    // 特别注意 status = 0 也算有值，0 是合法业务值（比如"禁用"状态）
                    return true;
                }

            } catch (NoSuchFieldException | IllegalAccessException e) {
                // 注解上写的字段名，在类里根本不存在 —— 这是写代码时的手误。
                // 这里选择「校验失败」让问题暴露出来；
                // 开发阶段你也可以改成 throw new IllegalArgumentException(e) 让它立刻炸。
                return false;
            }
        }

        // 所有字段都检查完了，一个有效值都没有 → 校验失败
        return false;
    }
}
