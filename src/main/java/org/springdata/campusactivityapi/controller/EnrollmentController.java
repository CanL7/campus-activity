package org.springdata.campusactivityapi.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdata.campusactivityapi.common.CurrentUser;
import org.springdata.campusactivityapi.common.Result;
import org.springdata.campusactivityapi.pojo.dto.EnrollDTO;
import org.springdata.campusactivityapi.pojo.dto.PageDTO;
import org.springdata.campusactivityapi.pojo.vo.EnrollmentVO;
import org.springdata.campusactivityapi.pojo.vo.PageVO;
import org.springdata.campusactivityapi.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    // 报名（POST /enrollments —— POST 本身就表示"新增一条报名"，不用再挂 /enroll）
    @PostMapping
    public Result<Long> enroll(@Valid @RequestBody EnrollDTO dto){
        Long userId = CurrentUser.getUserId();
        return Result.success(enrollmentService.enroll(dto,userId));//把报名记录id返回
    }

    // 取消报名：这是软删除（把 status 改成 2），不是真删记录，
    // 所以方法名用 cancel 而不是 delete —— 叫 delete 会让人以为记录被删了
    @PutMapping("/{enrollmentId}")
    public Result<Void> cancel(@PathVariable Long enrollmentId){
        // 路径里的 id 只负责"定位是哪一条"；"这条是不是你的"由 token 里的 userId 判定
        enrollmentService.updateStatus(enrollmentId, CurrentUser.getUserId());
        return Result.success();
    }

    // 支付
    @PutMapping("/{enrollmentId}/pay")
    public Result<Void> pay(@PathVariable Long enrollmentId){
        enrollmentService.pay(enrollmentId, CurrentUser.getUserId());
        return Result.success();
    }

    //分页查询我的报名
    @GetMapping("/my")//这里不能@RequestBody Get请求不带方法体的
    //这里直接采取query查询 从url里自动取自动装配 并记得加注解@Valid 防止乱传
    public Result<PageVO<EnrollmentVO>> mylist(@Valid PageDTO dto){
        Long userId = CurrentUser.getUserId();
        return Result.success(enrollmentService.selectMyList(userId,dto));
    }

}
