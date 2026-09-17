package org.springdata.campusactivityapi.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdata.campusactivityapi.annotation.PublicApi;
import org.springdata.campusactivityapi.annotation.RequireAdmin;
import org.springdata.campusactivityapi.common.Result;
import org.springdata.campusactivityapi.pojo.dto.ActivitiesDTO;
import org.springdata.campusactivityapi.pojo.dto.CreateActivityDTO;
import org.springdata.campusactivityapi.pojo.dto.UpdateActivityDTO;
import org.springdata.campusactivityapi.pojo.vo.ActivityVO;
import org.springdata.campusactivityapi.pojo.vo.PageVO;
import org.springdata.campusactivityapi.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/activities")
@Slf4j
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    //新增活动（POST /activities —— POST 本身就表示"新增"，不用再挂 /add）
    //★ 增删改都要管理员；下面的查询（GET）不加注解 = 公开可访问
    @RequireAdmin
    @PostMapping
    public Result<Void> createActivity(@Valid @RequestBody CreateActivityDTO dto){
        log.info("createActivity");
        activityService.addActivity(dto);
        return Result.success();
    }

    //查询活动（公开：不登录也能看，方便游客浏览活动）
    @PublicApi
    @GetMapping("/{activityId}")
    public Result<ActivityVO> getActivity(@PathVariable("activityId") Long id){
        log.info("getActivity");
        return Result.success(activityService.selectActivityById(id));
    }

    //全部查询
    @PublicApi
    @GetMapping
    public Result<PageVO<ActivityVO>> getActivities(@Valid ActivitiesDTO dto){
        log.info("getActivities");
        return Result.success(activityService.selectActivities(dto));
    }



    //改动活动
    @RequireAdmin
    @PutMapping("/{activityId}")
    public Result<Void> updateActivity(@PathVariable("activityId") Long id, @Valid @RequestBody UpdateActivityDTO dto){
        log.info("updateActivity");
        activityService.updateActivity(id,dto);
        return Result.success();
    }

    //删除活动
    @RequireAdmin
    @DeleteMapping("/{activityId}")
    public Result<Void> deleteActivity(@PathVariable("activityId") Long id){
        log.info("deleteActivity");
        activityService.deleteActivity(id);
        return Result.success();
    }









}
