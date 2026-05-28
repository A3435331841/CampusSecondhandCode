package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.secondhand.dto.StudentVerifyDTO;
import com.campus.secondhand.entity.StudentRoster;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.mapper.StudentRosterMapper;
import com.campus.secondhand.mapper.UserMapper;
import com.campus.secondhand.service.StudentVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class StudentVerificationServiceImpl implements StudentVerificationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StudentRosterMapper studentRosterMapper;

    @Override
    @Transactional
    public User verifyStudent(Long userId, StudentVerifyDTO dto) {
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new IllegalStateException("用户不存在");
        }

        QueryWrapper<StudentRoster> wrapper = new QueryWrapper<>();
        wrapper.eq("student_no", dto.getStudentNo())
                .eq("real_name", dto.getRealName())
                .eq("college", dto.getCollege())
                .eq("major", dto.getMajor())
                .eq("grade", dto.getGrade())
                .eq("id_card_suffix", dto.getIdCardSuffix())
                .eq("status", 1);
        StudentRoster roster = studentRosterMapper.selectOne(wrapper);
        if (roster == null) {
            throw new IllegalStateException("学籍认证失败");
        }

        LocalDateTime verifyTime = LocalDateTime.now();
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setRealName(dto.getRealName());
        updateUser.setStudentNo(dto.getStudentNo());
        updateUser.setCollege(dto.getCollege());
        updateUser.setMajor(dto.getMajor());
        updateUser.setGrade(dto.getGrade());
        updateUser.setIdCardSuffix(dto.getIdCardSuffix());
        updateUser.setVerifyStatus("VERIFIED");
        updateUser.setVerifyTime(verifyTime);
        userMapper.updateById(updateUser);

        currentUser.setRealName(updateUser.getRealName());
        currentUser.setStudentNo(updateUser.getStudentNo());
        currentUser.setCollege(updateUser.getCollege());
        currentUser.setMajor(updateUser.getMajor());
        currentUser.setGrade(updateUser.getGrade());
        currentUser.setIdCardSuffix(updateUser.getIdCardSuffix());
        currentUser.setVerifyStatus(updateUser.getVerifyStatus());
        currentUser.setVerifyTime(updateUser.getVerifyTime());
        return currentUser;
    }
}
