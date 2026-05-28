package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_student_roster")
public class StudentRoster {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentNo;
    private String realName;
    private String college;
    private String major;
    private String grade;
    private String idCardSuffix;
    private Integer status;
    private LocalDateTime createTime;
}
