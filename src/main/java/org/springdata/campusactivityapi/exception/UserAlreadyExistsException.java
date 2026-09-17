package org.springdata.campusactivityapi.exception;

public class UserAlreadyExistsException extends BusinessException{
    public UserAlreadyExistsException(String message)
    {
        //409 资源冲突
        super(409, message);
    }
}
