package com.campus.secondhand.controller;

import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.StudentVerifyDTO;
import com.campus.secondhand.entity.User;
import com.campus.secondhand.service.StudentVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    @Autowired
    private StudentVerificationService studentVerificationService;

    @PostMapping("/student")
    public Result<Map<String, Object>> verifyStudent(@RequestBody StudentVerifyDTO dto, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        try {
            User verifiedUser = studentVerificationService.verifyStudent(currentUserId, dto);
            Map<String, Object> result = new HashMap<>();
            result.put("userId", verifiedUser.getId());
            result.put("nickname", verifiedUser.getNickname());
            result.put("avatar", verifiedUser.getAvatar());
            result.put("role", verifiedUser.getRole());
            result.put("verifyStatus", verifiedUser.getVerifyStatus());
            result.put("realName", verifiedUser.getRealName());
            result.put("studentNo", verifiedUser.getStudentNo());
            result.put("college", verifiedUser.getCollege());
            result.put("major", verifiedUser.getMajor());
            result.put("grade", verifiedUser.getGrade());
            return Result.success(result);
        } catch (IllegalStateException exception) {
            return Result.error(exception.getMessage());
        }
    }
}
