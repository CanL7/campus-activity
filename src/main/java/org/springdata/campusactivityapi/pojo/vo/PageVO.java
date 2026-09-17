package org.springdata.campusactivityapi.pojo.vo;


import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {//写为泛型 可以同时给activity和enrollment用

    private List<T> list;//数据
    private Integer pageNum;//页码
    private Integer pageSize;//每页几条
    private Integer totalPage;//总页数
    private Long total;//总条数

}
