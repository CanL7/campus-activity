package org.springdata.campusactivityapi.service;


import jakarta.validation.Valid;
import org.springdata.campusactivityapi.pojo.dto.EnrollDTO;
import org.springdata.campusactivityapi.pojo.dto.PageDTO;
import org.springdata.campusactivityapi.pojo.vo.EnrollmentVO;
import org.springdata.campusactivityapi.pojo.vo.PageVO;

public interface EnrollmentService {

    Long enroll(@Valid EnrollDTO dto,Long  userId);

    void updateStatus(Long id, Long userId);

    void scanExpiredEnrollment();

    void pay(Long enrollmentId, Long userId);


    PageVO<EnrollmentVO> selectMyList(Long userId, PageDTO dto);
}
