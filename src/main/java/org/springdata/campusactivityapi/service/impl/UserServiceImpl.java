package org.springdata.campusactivityapi.service.impl;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdata.campusactivityapi.exception.*;
import org.springdata.campusactivityapi.mapper.UserMapper;
import org.springdata.campusactivityapi.pojo.dto.LoginDTO;
import org.springdata.campusactivityapi.pojo.dto.UpdateUserDTO;
import org.springdata.campusactivityapi.pojo.vo.LoginVO;
import org.springdata.campusactivityapi.pojo.vo.UserVO;
import org.springdata.campusactivityapi.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springdata.campusactivityapi.pojo.dto.CreateUserDTO;
import org.springdata.campusactivityapi.pojo.entity.User;
import org.springdata.campusactivityapi.service.UserService;

import java.util.List;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;   // 登录成功后用它签发 token

    @Override
    public void saveUser(CreateUserDTO dto) {

        User user = new User();
        BeanUtils.copyProperties(dto,user,"password");
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);//启用

        try {
            userMapper.insertUser(user);
        }catch (DuplicateKeyException e) {
            throw new UserAlreadyExistsException("该用户名已存在");
        }

    }

    @Override
    public UserVO selectUser(Long id) {

        UserVO userVO = userMapper.selectUserById(id);
        if(userVO == null) {
            throw new UserNotFoundException("该用户不存在");
        }
        return userVO;

    }

    @Override
    public void deleteUser(Long id) {
        int affectrows = userMapper.deleteUserById(id);
        if(affectrows == 0) {
            throw new UserNotFoundException("该用户不存在");
        }
    }

    @Override
    public void updateUser(Long id, UpdateUserDTO updateUserDTO) {
        if(updateUserDTO.getNickName() == null && updateUserDTO.getStatus() == null) {
            throw new BusinessException( 500 ,"不能都输入为空");
        }
        User user = new User();
        user.setId(id);
        BeanUtils.copyProperties(updateUserDTO,user);
        int affectrows = userMapper.updateUser(user);
        if(affectrows == 0) {
            throw new UserNotFoundException("该用户不存在");
        }
    }

    //用户登录 密码已经Crypt加密过
    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectUserByUsername(dto.getUserName());
        if(user == null) {
            // 用户不存在也走 401、也用同一句话：否则攻击者靠状态码(404 vs 401)
            // 就能批量枚举出哪些用户名真实存在
            throw new WrongPasswordException("用户名或密码错误");
        }
        //拿到数据库密文
        //第一个是明文 第二个是数据库里的哈希
        boolean ismatched = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        if(!ismatched) {
            throw new WrongPasswordException("用户名或密码错误");
        }
        if(user.getStatus() == 0) {
            throw new UserLockedException("用户状态异常,请联系管理员");
        }
        //登陆成功 我们要给vo赋值
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);

        //用 userId 签一个 token 一起返回 —— 之后前端每个请求都要带它
        //★ role 一起放进 token：之后拦截器判断"是不是管理员"就不用再查库了
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(jwtUtil.generate(user.getId(), user.getRole()));
        loginVO.setUser(userVO);
        return loginVO;
    }
}