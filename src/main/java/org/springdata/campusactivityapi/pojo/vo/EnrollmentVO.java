package org.springdata.campusactivityapi.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnrollmentVO {


    private Long id;

    private Long activityId;

    //实时查询 因为活动名改变这里查询也要变
    private String title;//活动名
    private String location;//活动位置

    private Long userId;

    /** 0待支付 1已确认 2已取消 */
    private Integer status;

    /**
     * 支付截止时间。
     * 超过这个时间还是 0待支付 的话，名额要被释放回活动。
     */
    private LocalDateTime expireTime;

    private LocalDateTime createTime;//创建时间 用于排序
}
