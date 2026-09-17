package org.springdata.campusactivityapi.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springdata.campusactivityapi.exception.ActivityNotFoundException;
import org.springdata.campusactivityapi.mapper.ActivityMapper;
import org.springdata.campusactivityapi.pojo.dto.ActivitiesDTO;
import org.springdata.campusactivityapi.pojo.dto.CreateActivityDTO;
import org.springdata.campusactivityapi.pojo.dto.UpdateActivityDTO;
import org.springdata.campusactivityapi.pojo.entity.Activity;
import org.springdata.campusactivityapi.pojo.vo.ActivityVO;
import org.springdata.campusactivityapi.pojo.vo.PageVO;
import org.springdata.campusactivityapi.service.ActivityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;


    @Override
    public void addActivity(CreateActivityDTO dto) {
        Activity a = new Activity();

        BeanUtils.copyProperties(dto, a);
        a.setEnrolledCount(0);
        a.setStatus(0);
        activityMapper.insertActivity(a);

    }

    @Override
    public ActivityVO selectActivityById(Long id) {
        ActivityVO activityVO = activityMapper.selectActivityById(id);
        if (activityVO == null) {
            throw new ActivityNotFoundException("活动不存在");
        }
        return activityVO;
    }

    @Override
    public void updateActivity(Long id,UpdateActivityDTO dto) {

        Activity a = new Activity();
        a.setId(id);
        BeanUtils.copyProperties(dto, a);

        int affectrows = activityMapper.updateActivity(a);
        if(affectrows == 0 ) {
            throw new ActivityNotFoundException("活动不存在");
        }

    }

    @Override
    public void deleteActivity(Long id) {
        int affectrows = activityMapper.deleteActivity(id);
        if(affectrows == 0 ) {
            throw new ActivityNotFoundException("活动不存在");
        }
    }

    @Override
    public PageVO<ActivityVO> selectActivities(ActivitiesDTO dto) {
        PageInfo<ActivityVO> pageInfo = PageHelper.startPage(dto.getPageNum(), dto.getPageSize())
                .doSelectPageInfo(() -> activityMapper.selectActivities(dto));

        PageVO<ActivityVO> pageVO = new PageVO<>();
        pageVO.setPageNum(pageInfo.getPageNum());
        pageVO.setTotalPage(pageInfo.getPages());
        pageVO.setTotal(pageInfo.getTotal());
        pageVO.setList(pageInfo.getList());
        pageVO.setPageSize(pageInfo.getPageSize());
        return pageVO;
    }
}
