package org.springdata.campusactivityapi.pojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateActivityDTO {
    //private Long id;

    @NotBlank(message = "活动标题不能为空")
    private String title;//标题

    private String description;//描述

    private String location;//活动位置

    @NotNull(message = "活动开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    private LocalDateTime endTime;

    /** 总名额，报名上限 */
    @NotNull(message = "总名额不能为空")
    @Min(value = 1, message = "总名额至少为 1")
    private Integer capacity;

    /** 0未发布 1已发布 2已取消 */
    //private Integer status;


}
