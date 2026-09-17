package org.springdata.campusactivityapi.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {//基础业务异常

    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);//调用父类的方法 把detailMessage设好 这是标准
        this.code = code;
    }
}
