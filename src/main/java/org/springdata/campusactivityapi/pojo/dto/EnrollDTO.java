package org.springdata.campusactivityapi.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnrollDTO {

    @NotNull
    private Long activityId;


}
