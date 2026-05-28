package com.campus.secondhand.dto;

import lombok.Data;

@Data
public class StudentVerifyDTO {
    private String realName;
    private String studentNo;
    private String college;
    private String major;
    private String grade;
    private String idCardSuffix;
}
