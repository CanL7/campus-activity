package org.springdata.campusactivityapi.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Activity {

    private Long id;

    private String title;//标题

    private String description;//描述

    private String location;//活动位置

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 总名额，报名上限 */
    private Integer capacity;

    /**
     * 已报名人数，冗余字段。
     * 不用每次去报名表 count(*)，报名接口是高并发热点，扛不住全表统计。
     * 它和 capacity 配合，靠原子 UPDATE 防超卖。
     */
    private Integer enrolledCount;

    /** 0未发布 1已发布 2已取消 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
