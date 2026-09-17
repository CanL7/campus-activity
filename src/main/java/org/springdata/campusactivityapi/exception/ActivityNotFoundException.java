package org.springdata.campusactivityapi.exception;

public class ActivityNotFoundException extends BusinessException {
    public ActivityNotFoundException(String message) {
        // 404 资源不存在
        super(404, message);
    }
}