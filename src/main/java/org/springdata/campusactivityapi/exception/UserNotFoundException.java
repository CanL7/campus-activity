package org.springdata.campusactivityapi.exception;

public class UserNotFoundException extends BusinessException{
    public UserNotFoundException(String message)
    {
        //404 资源不存在
        super(404, message);
    }
}
