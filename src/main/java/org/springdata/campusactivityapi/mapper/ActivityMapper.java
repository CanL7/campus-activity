package org.springdata.campusactivityapi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springdata.campusactivityapi.pojo.dto.ActivitiesDTO;
import org.springdata.campusactivityapi.pojo.entity.Activity;
import org.springdata.campusactivityapi.pojo.vo.ActivityVO;

import java.util.List;

@Mapper
public interface ActivityMapper {

    void insertActivity(Activity a);

    ActivityVO selectActivityById(Long id);

    int updateActivity(Activity a);

    /**
     * 原子化
     * @param id
     * @return
     */
    int tryEnroll(@Param("id") Long id);

    int deleteActivity(Long id);

    void rollbackCountBatch(@Param("enrollmentIds") List<Long> enrollmentIds);

    void rollbackCount(@Param("enrollmentId")Long enrollmentId);

    List<ActivityVO> selectActivities(ActivitiesDTO dto);
}