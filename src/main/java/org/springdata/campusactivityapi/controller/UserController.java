package org.springdata.campusactivityapi.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdata.campusactivityapi.common.Result;
import org.springdata.campusactivityapi.pojo.dto.CreateUserDTO;
import org.springdata.campusactivityapi.pojo.dto.UpdateUserDTO;
import org.springdata.campusactivityapi.pojo.vo.UserVO;
import org.springdata.campusactivityapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资源的增删改查。
 * 注意：注册 / 登录已移到 AuthController（/auth/**），因为它们的权限边界不同 --
 * 这里是"管理用户"，以后应该只有管理员能调；认证接口是匿名可访问的。
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    //增加新用户（POST /users 本身就是"新增"，不需要再挂 /add）
    @PostMapping
    public Result<String> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        log.info("createUser");
        userService.saveUser(createUserDTO);
        return Result.success();
    }

    //查询用户
    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        log.info("getUser");
        UserVO userVO = userService.selectUser(id);
        return Result.success(userVO);
    }

    //删除用户
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        log.info("deleteUser");
        userService.deleteUser(id);
        return Result.success();
    }

    //改动用户
    @PutMapping("/{id}")
    public Result<String> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        log.info("updateUser");
        userService.updateUser(id, updateUserDTO);
        return Result.success();
    }
}
