package org.springdata.campusactivityapi.pojo.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PageDTO {

    @NotNull
    private Integer pageNum = 1;//页码
    @NotNull
    @Max(message = "单页最多100条",value = 100)
    private Integer pageSize = 10;//每页几条
}
