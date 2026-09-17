package org.springdata.campusactivityapi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springdata.campusactivityapi.pojo.entity.ActivityEnrollment;
import org.springdata.campusactivityapi.pojo.vo.EnrollmentVO;

import java.util.List;

@Mapper
public interface EnrollmentMapper {

    int insert(ActivityEnrollment ae);

    int updateStatusById(@Param("id") Long id, @Param("userId") Long userId);

    List<ActivityEnrollment> selectExpiredEnrollment();

    void markExpiredBatch(@Param("ids") List<Long> ids);

    int updateStatusAfterPay(@Param("id") Long enrollmentId, @Param("userId") Long userId);


    List<EnrollmentVO> selectPage(@Param("userId") Long userId);
}
