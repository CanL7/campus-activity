package org.springdata.campusactivityapi.pojo.entity;

import lombok.Data;

@Data
public class User {
    private Long id;
    private Integer status;//1启用 0 禁用
    private Integer role;//0普通用户 1管理员
    private String userName;//每个用户唯一
    private String password;
    private String nickName;
}
