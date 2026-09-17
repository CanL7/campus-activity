package org.springdata.campusactivityapi.exception;

public class EnrollmentNotPayableException extends BusinessException{
    public EnrollmentNotPayableException(String message) {
        super(409, message);
    }
}
