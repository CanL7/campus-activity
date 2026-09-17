package org.springdata.campusactivityapi.pojo.vo;

import lombok.Data;

/**
 * 登录成功的返回：凭证 + 用户信息。
 *
 * token 为什么不塞进 UserVO：
 *   UserVO 在 GET /users/{id} 也会用到，那个接口不该返回 token。
 *   凭证和业务数据分开两个 VO，各管各的。
 */
@Data
public class LoginVO {

    private String token;
    private UserVO user;
}
