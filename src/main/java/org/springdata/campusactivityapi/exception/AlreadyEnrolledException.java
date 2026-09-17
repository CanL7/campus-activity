package org.springdata.campusactivityapi.exception;

public class AlreadyEnrolledException extends BusinessException {
    public AlreadyEnrolledException(String message) {
        super(409,message);
    }
}
