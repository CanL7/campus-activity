package org.springdata.campusactivityapi.service;

import org.springdata.campusactivityapi.pojo.dto.CreateUserDTO;
import org.springdata.campusactivityapi.pojo.dto.LoginDTO;
import org.springdata.campusactivityapi.pojo.dto.UpdateUserDTO;
import org.springdata.campusactivityapi.pojo.vo.LoginVO;
import org.springdata.campusactivityapi.pojo.vo.UserVO;

public interface UserService {

    /**
     * 创建用户
     * @param createUserDTO
     */
     void saveUser(CreateUserDTO createUserDTO);


    /**
     *查询用户
     * @param id
     * @return
     */
    UserVO selectUser(Long id);


    /**
     * 删除用户
     * @param id
     */
    void deleteUser(Long id);

    /**
     * 更新用户
     * @param id
     * @param updateUserDTO
     */
    void updateUser(Long id, UpdateUserDTO updateUserDTO);

    /**
     * 用户登录
     * @param loginDTO
     */
    LoginVO login(LoginDTO loginDTO);
}
