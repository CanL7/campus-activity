package org.springdata.campusactivityapi.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityEnrollment {

    private Long id;

    private Long activityId;

    private Long userId;


    /** 0待支付 1已确认 2已取消 */
    private Integer status;

    /**
     * 支付截止时间。
     * 超过这个时间还是 0待支付 的话，名额要被释放回活动。
     */
    private LocalDateTime expireTime;

    private LocalDateTime createTime;
}
