package com.campus.secondhand.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long fromUserId;
    private Long toUserId;
    private Long productId;
    private String content;
    /** text/system */
    private String type;
    private Long timestamp;
}
