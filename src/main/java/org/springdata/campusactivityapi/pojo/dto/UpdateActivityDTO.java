package org.springdata.campusactivityapi.pojo.dto;

import lombok.Data;
import org.springdata.campusactivityapi.validation.AtLeastOneField;

import java.time.LocalDateTime;

@Data
@AtLeastOneField(fields = {"title","description","location","startTime","endTime","capacity","status"})
public class UpdateActivityDTO {
    private String title;//标题

    private String description;//描述

    private String location;//活动位置

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 总名额，报名上限 */
    private Integer capacity;

    /** 0未发布 1已发布 2已取消 */
    private Integer status;

}
