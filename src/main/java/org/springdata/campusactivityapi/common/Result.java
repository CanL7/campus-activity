package org.springdata.campusactivityapi.common;

import lombok.Data;

@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    /*
    static = 类级别
    普通成员 = 对象级别
    static没有this 因为不知道对应的是哪个对象 而且还可能都没有对象
     */

    //带数据的成功
    //这里的第一个<T> 是静态方法如果要用泛型 就必须要自己再声明一个静态泛型参数
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message,T data) {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    //不带数据的成功
    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(null);
        return result;
    }
    //不带数据失败
    public static <T> Result<T> error() {
        Result<T> result = new Result<T>();
        result.setCode(500);
        result.setMessage("error");
        result.setData(null);
        return result;
    }

    //不带数据失败
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<T>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
    //带数据失败
    public static <T> Result<T> error(Integer code, String message,T data) {
        Result<T> result = new Result<T>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }


}
