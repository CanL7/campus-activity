package org.springdata.campusactivityapi.exception;

/**
 * 已认证但权限不足。
 *
 * 401 和 403 的区别（面试常问）：
 *   401 Unauthorized —— 其实是"未认证"：没带 token、token 过期、签名不对。
 *                       语义是"你是谁我不知道，请先证明身份"。
 *   403 Forbidden    —— "已认证但无权限"：我知道你是 carol，但 carol 不是管理员。
 *                       这时重新登录也没用，换账号才行 —— 所以不能返回 401。
 *
 * 名字虽然叫 Unauthorized（HTTP 规范的历史遗留命名），语义上它管的是"没登录"。
 */
public class ForbiddenException extends BusinessException {
    public ForbiddenException(String message) {
        super(403, message);
    }
}
