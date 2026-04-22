package com.campus.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionKey;
    private Long productId;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private String type;
    private LocalDateTime createTime;
}
