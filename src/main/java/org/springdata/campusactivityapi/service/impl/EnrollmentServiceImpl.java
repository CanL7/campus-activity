package org.springdata.campusactivityapi.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springdata.campusactivityapi.exception.ActivityNotFoundException;
import org.springdata.campusactivityapi.exception.AlreadyEnrolledException;
import org.springdata.campusactivityapi.exception.BusinessException;
import org.springdata.campusactivityapi.exception.EnrollmentFullException;
import org.springdata.campusactivityapi.exception.EnrollmentNotPayableException;
import org.springdata.campusactivityapi.mapper.ActivityMapper;
import org.springdata.campusactivityapi.mapper.EnrollmentMapper;
import org.springdata.campusactivityapi.pojo.dto.EnrollDTO;
import org.springdata.campusactivityapi.pojo.dto.PageDTO;
import org.springdata.campusactivityapi.pojo.entity.ActivityEnrollment;
import org.springdata.campusactivityapi.pojo.vo.ActivityVO;
import org.springdata.campusactivityapi.pojo.vo.EnrollmentVO;
import org.springdata.campusactivityapi.pojo.vo.PageVO;
import org.springdata.campusactivityapi.service.EnrollmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentMapper enrollmentMapper;

    @Autowired
    private ActivityMapper activityMapper;

    /**
     * 报名。并发安全靠两处：tryEnroll 的原子 UPDATE（抢名额）+ 唯一索引（防重复报名）。
     * @Transactional 保证「抢名额 + 插记录」同生共死：插记录失败时名额会回滚。
     * 注意：@Transactional 本身不加锁、挡不住超卖 —— 挡超卖的是 tryEnroll 的 WHERE 条件。
     */
    @Transactional
    @Override
    public Long enroll(EnrollDTO dto, Long userId) {

        // 查活动
        ActivityVO vo = activityMapper.selectActivityById(dto.getActivityId());
        if (vo == null) {
            throw new ActivityNotFoundException("该活动不存在");
        }
        // 尝试注册 tryEnroll
        int affectrows = activityMapper.tryEnroll(dto.getActivityId());
        if (affectrows == 0) {
            throw new EnrollmentFullException("报名人数已满");
        }
        // 插报名记录
        ActivityEnrollment enrollment = new ActivityEnrollment();
        BeanUtils.copyProperties(dto, enrollment);
        enrollment.setUserId(userId);
        enrollment.setStatus(0);                                        // 0 = 待支付
        enrollment.setExpireTime(LocalDateTime.now().plusMinutes(15));  // 15 分钟后过期
        try{
            enrollmentMapper.insert(enrollment);
        }catch(DuplicateKeyException exception){
            throw new AlreadyEnrolledException("您已经报名过此活动");
        }
        return enrollment.getId();
    }
    //取消报名（软删除 改成状态2）—— 只能取消自己的
    @Transactional
    @Override
    public void updateStatus(Long id, Long userId) {
        // 影响行数 = 0 说明三者之一：这条不存在 / 不是你的 / 已经不是待支付状态
        int affected = enrollmentMapper.updateStatusById(id, userId);
        if (affected == 0) {
            // 用 404 而不是 403：403 等于告诉攻击者"这个 id 存在，只是不属于你"
            throw new BusinessException(404, "报名记录不存在");
        }
        // 抢到作废权了，才还名额（两步写操作，所以方法上要有 @Transactional）
        activityMapper.rollbackCount(id);
    }

    //定时任务扫描过期未支付报名表
    @Override
    @Scheduled(cron = "0/30 * * * * ?")
    public void scanExpiredEnrollment() {
        //扫描并接住过期表 这里是status = 0
        List<ActivityEnrollment> expiredEnrollments = enrollmentMapper.selectExpiredEnrollment();
        /**
         * 处理过期表 也要用 status = 0
         * 解释一下 因为 接住的时候 跟现在处理之间有时间间隔 如果支付和现在同时进行 就有可能发生业务错误
         * 白收钱是不对的(
         */
        //流式处理
        List<Long> enrollmentIds = expiredEnrollments.stream().map(ActivityEnrollment::getId).toList();
        //处理一下 防止传空集合
        if (expiredEnrollments.isEmpty()) {
            return;
        }
        //正式处理并标记 status为2
        enrollmentMapper.markExpiredBatch(enrollmentIds);
        //回退enrolled_count
        activityMapper.rollbackCountBatch(enrollmentIds);
    }

    @Transactional
    @Override
    public void pay(Long enrollmentId, Long userId) {
        // 三个守卫：status = 0（防重复支付 + 抢同一行）、expire_time > now()（补定时任务的超时窗口）、
        // user_id = #{userId}（只能支付自己的报名）
        int affectrows = enrollmentMapper.updateStatusAfterPay(enrollmentId, userId);
        if(affectrows == 0){
            throw new EnrollmentNotPayableException("报名不存在或当前状态不可支付");
        }
    }

    @Override
    public PageVO<EnrollmentVO> selectMyList(Long userId, PageDTO dto) {
        //启动pagehelper来管理sql
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        //拿普通list来获得page数据
        List<EnrollmentVO> list = enrollmentMapper.selectPage(userId);
        //用pageinfo暂时封装数据
        PageInfo<EnrollmentVO> pageInfo = new PageInfo<>(list);
        //返回类型是VO 因为PageInfo 太多无用字段了
        PageVO<EnrollmentVO> pageVO = new PageVO<>();
        //给VO赋值
        pageVO.setList(pageInfo.getList());
        pageVO.setPageSize(pageInfo.getPageSize());
        pageVO.setPageNum(pageInfo.getPageNum());
        pageVO.setTotalPage(pageInfo.getPages());
        pageVO.setTotal(pageInfo.getTotal());

        return  pageVO;
    }


}
