package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatar;
    private String realName;
    private String studentNo;
    private String college;
    private String major;
    private String grade;
    private String idCardSuffix;
    private String verifyStatus;
    private LocalDateTime verifyTime;
    private LocalDateTime lastLoginTime;
    private Integer creditScore;
    private Integer favoriteCount;
    private BigDecimal buyRatingAvg;
    private BigDecimal sellRatingAvg;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
}
