package org.springdata.campusactivityapi.service;


import jakarta.validation.Valid;
import org.springdata.campusactivityapi.pojo.dto.ActivitiesDTO;
import org.springdata.campusactivityapi.pojo.dto.CreateActivityDTO;
import org.springdata.campusactivityapi.pojo.dto.UpdateActivityDTO;
import org.springdata.campusactivityapi.pojo.vo.ActivityVO;
import org.springdata.campusactivityapi.pojo.vo.PageVO;


public interface ActivityService {
    /**
     * 新建活动
     * @param dto
     */
    void addActivity(CreateActivityDTO dto);

    /**
     * 查询
     * @param id
     * @return
     */
    ActivityVO selectActivityById(Long id);

    /**
     * 更改活动
     * @param id
     * @param dto
     */
    void updateActivity(Long id,UpdateActivityDTO dto);

    /**
     * 删除活动
     * @param id
     */
    void deleteActivity(Long id);

    /**
     * 查询所有活动
     * @param dto
     * @return
     */
    PageVO<ActivityVO> selectActivities(@Valid ActivitiesDTO dto);
}
