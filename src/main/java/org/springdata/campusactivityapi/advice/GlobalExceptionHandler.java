package org.springdata.campusactivityapi.advice;

import lombok.extern.slf4j.Slf4j;
import org.springdata.campusactivityapi.common.Result;
import org.springdata.campusactivityapi.exception.BusinessException;
import org.springdata.campusactivityapi.pojo.vo.FieldErrorVO;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception){
        log.warn("BusinessException: {}", exception.getMessage());
        return Result.error(exception.getCode(), exception.getMessage());
    }

    /**
     * 参数校验失败。
     * 必须用 getAllErrors()：类级别约束（@AtLeastOneField）产生的是 ObjectError，
     * 不在 getFieldErrors() 里面，用错就拿不到自定义 message。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<List<FieldErrorVO>> handleValidException(MethodArgumentNotValidException exception){
        List<FieldErrorVO> errors = exception.getBindingResult().getAllErrors().stream()
                .filter(error -> error.getDefaultMessage() != null)//先过滤 可以减少转换工作量
                .map(e -> new FieldErrorVO(
                        //重点判断 只有fielderror能拿到field信息 所以先判断然后转换
                        e instanceof FieldError fe ? fe.getField() : null, e.getDefaultMessage()
                )).toList();

        //直接把错误信息汇总 一行输出
        String summary = errors.stream()
                .map(FieldErrorVO::getMessage)
                .collect(Collectors.joining(", "));

        log.warn("参数校验失败 : {}", summary);

        return Result.error(400,summary.isEmpty() ? "参数校验异常" : summary , errors);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException exception){
        log.warn("NoResourceFoundException: {}", exception.getMessage());
        return Result.error(404,"请求资源不存在");
    }

    //最终兜底
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception){
        // 传异常对象本身，SLF4J 才会打印完整堆栈；只传 getMessage() 线上没法排查
        log.error("未预期异常", exception);
        return Result.error(500, "服务器内部错误");
    }
}
