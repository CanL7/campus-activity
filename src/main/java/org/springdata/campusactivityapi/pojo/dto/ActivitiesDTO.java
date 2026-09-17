package org.springdata.campusactivityapi.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
// 继承 PageDTO，所以 equals/hashCode 要把父类的 pageNum/pageSize 也算进去
// （不加这行 Lombok 只比较本类字段，且编译时会警告）
@EqualsAndHashCode(callSuper = true)
public class ActivitiesDTO extends PageDTO {

    private Integer status;
    private String keyword;
}
