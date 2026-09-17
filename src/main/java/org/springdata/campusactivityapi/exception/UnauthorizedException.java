package org.springdata.campusactivityapi.exception;

/**
 * 未认证 / 凭证无效。
 *
 * 401 和 403 的区别：
 *   401 = 我不知道你是谁（没登录、token 过期）
 *   403 = 我知道你是谁，但你没权限（登录了但不是管理员）
 */
public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(401, message);
    }
}