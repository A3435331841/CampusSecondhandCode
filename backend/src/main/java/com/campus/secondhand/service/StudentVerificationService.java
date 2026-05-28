package com.campus.secondhand.service;

import com.campus.secondhand.dto.StudentVerifyDTO;
import com.campus.secondhand.entity.User;

public interface StudentVerificationService {
    User verifyStudent(Long userId, StudentVerifyDTO dto);
}
