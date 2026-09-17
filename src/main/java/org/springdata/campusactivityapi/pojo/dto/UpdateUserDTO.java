package org.springdata.campusactivityapi.pojo.dto;

import lombok.Data;
import org.springdata.campusactivityapi.validation.AtLeastOneField;

@Data
@AtLeastOneField(fields = {"nickName", "status"}, message = "nickName 和 status 至少填一个")
public class UpdateUserDTO {

    private String nickName;
    private Integer status;
}
