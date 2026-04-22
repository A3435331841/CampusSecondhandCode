package com.campus.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.dto.ChatMessageDTO;

public interface ChatService {
    void saveMessage(ChatMessageDTO dto);

    IPage<ChatMessageDTO> listHistory(Long currentUserId, Long toUserId, Long productId, Integer current, Integer size);
}
