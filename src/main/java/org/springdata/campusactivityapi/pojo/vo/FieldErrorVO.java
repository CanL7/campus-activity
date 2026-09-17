package org.springdata.campusactivityapi.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class FieldErrorVO {
    //字段名
    private String field;
    //错误信息
    private String message;
}
