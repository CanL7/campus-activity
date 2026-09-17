package org.springdata.campusactivityapi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springdata.campusactivityapi.pojo.entity.User;
import org.springdata.campusactivityapi.pojo.vo.UserVO;

@Mapper
public interface UserMapper {


    void insertUser(User user);

    User selectUserByUsername(String userName);

    UserVO selectUserById(Long id);

    int deleteUserById(Long id);

    int updateUser(User user);
}