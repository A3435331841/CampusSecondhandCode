package com.campus.secondhand.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSessionDTO {
    private Long sessionId;
    private String sessionKey;
    private Long productId;
    private Long peerUserId;
    private String peerNickname;
    private String peerAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}
