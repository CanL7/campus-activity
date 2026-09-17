package org.springdata.campusactivityapi.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdata.campusactivityapi.common.Result;
import org.springdata.campusactivityapi.pojo.dto.CreateUserDTO;
import org.springdata.campusactivityapi.pojo.dto.LoginDTO;
import org.springdata.campusactivityapi.pojo.vo.LoginVO;
import org.springdata.campusactivityapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关接口：注册、登录。
 *
 * 单独成一个 Controller（而不是塞进 UserController），是因为权限边界不同：
 * /auth/** 是匿名可访问的，/users/** 以后应该只有管理员能调。
 * 以后接 JWT + 拦截器时，白名单只需要写一行 /auth/**。
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private UserService userService;

    // 注册
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody CreateUserDTO dto) {
        log.info("register: {}", dto.getUserName());//只记用户名，绝不能记密码
        userService.saveUser(dto);
        return Result.success();
    }

    // 登录
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        log.info("login: {}", dto.getUserName());
        // 用 POST 而不是 GET：GET 的参数必须放 URL，密码会被浏览器历史、
        // 服务器访问日志、代理日志、Referer 头记录；POST 的参数在请求体里，不落这些地方
        // 返回体里带 token：前端存下来，之后每个请求用 Authorization: Bearer <token> 带上
        return Result.success(userService.login(dto));
    }
}
