package org.springdata.campusactivityapi.exception;

public class WrongPasswordException extends BusinessException {
    public WrongPasswordException(String message) {
        super(401,message);
    }
}
