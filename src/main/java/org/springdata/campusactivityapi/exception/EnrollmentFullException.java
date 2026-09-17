package org.springdata.campusactivityapi.exception;


public class EnrollmentFullException extends BusinessException {

    public EnrollmentFullException(String message){
        super(409, message);

    }
}
