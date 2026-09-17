package org.springdata.campusactivityapi.exception;

public class UserLockedException extends BusinessException {
    public UserLockedException(String message) {
        super(409,message);
    }
}
