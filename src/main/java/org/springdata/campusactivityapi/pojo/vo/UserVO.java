package org.springdata.campusactivityapi.pojo.vo;

import lombok.Data;

//安全 不传密码
@Data
public class UserVO {

    private Integer status;
    private Integer role;//0普通用户 1管理员 —— 前端靠它决定要不要显示"管理"按钮
    private String userName;//每个用户唯一
    private String nickName;

}
