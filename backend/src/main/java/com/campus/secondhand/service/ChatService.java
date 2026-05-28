package com.campus.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.dto.ChatMessageDTO;
import com.campus.secondhand.dto.ChatSessionDTO;

public interface ChatService {
    void saveMessage(ChatMessageDTO dto);

    IPage<ChatMessageDTO> listHistory(Long currentUserId, Long toUserId, Long productId, Integer current, Integer size);

    IPage<ChatSessionDTO> listSessions(Long userId, Integer current, Integer size);
}
