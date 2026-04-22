package com.campus.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.secondhand.dto.ChatMessageDTO;
import com.campus.secondhand.entity.ChatMessage;
import com.campus.secondhand.entity.ChatSession;
import com.campus.secondhand.mapper.ChatMessageMapper;
import com.campus.secondhand.mapper.ChatSessionMapper;
import com.campus.secondhand.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    private String buildSessionKey(Long a, Long b, Long productId) {
        long min = Math.min(a, b);
        long max = Math.max(a, b);
        return productId + "_" + min + "_" + max;
    }

    @Override
    @Transactional
    public void saveMessage(ChatMessageDTO dto) {
        String sessionKey = buildSessionKey(dto.getFromUserId(), dto.getToUserId(), dto.getProductId());
        LocalDateTime now = LocalDateTime.now();

        QueryWrapper<ChatSession> sessionWrapper = new QueryWrapper<>();
        sessionWrapper.eq("session_key", sessionKey);
        ChatSession session = chatSessionMapper.selectOne(sessionWrapper);
        if (session == null) {
            session = new ChatSession();
            session.setSessionKey(sessionKey);
            session.setProductId(dto.getProductId());
            session.setUserAId(Math.min(dto.getFromUserId(), dto.getToUserId()));
            session.setUserBId(Math.max(dto.getFromUserId(), dto.getToUserId()));
            session.setCreateTime(now);
            session.setLastMessageTime(now);
            chatSessionMapper.insert(session);
        } else {
            session.setLastMessageTime(now);
            chatSessionMapper.updateById(session);
        }

        ChatMessage message = new ChatMessage();
        message.setSessionKey(sessionKey);
        message.setProductId(dto.getProductId());
        message.setFromUserId(dto.getFromUserId());
        message.setToUserId(dto.getToUserId());
        message.setContent(dto.getContent());
        message.setType(dto.getType());
        message.setCreateTime(now);
        chatMessageMapper.insert(message);
    }

    @Override
    public IPage<ChatMessageDTO> listHistory(Long currentUserId, Long toUserId, Long productId, Integer current, Integer size) {
        String sessionKey = buildSessionKey(currentUserId, toUserId, productId);
        Page<ChatMessage> page = new Page<>(current, size);
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("session_key", sessionKey).orderByAsc("id");
        IPage<ChatMessage> messagePage = chatMessageMapper.selectPage(page, wrapper);

        List<ChatMessageDTO> records = new ArrayList<>();
        for (ChatMessage message : messagePage.getRecords()) {
            ChatMessageDTO dto = new ChatMessageDTO();
            dto.setFromUserId(message.getFromUserId());
            dto.setToUserId(message.getToUserId());
            dto.setProductId(message.getProductId());
            dto.setContent(message.getContent());
            dto.setType(message.getType());
            dto.setTimestamp(message.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
            records.add(dto);
        }
        Page<ChatMessageDTO> result = new Page<>(current, size);
        result.setTotal(messagePage.getTotal());
        result.setRecords(records);
        return result;
    }
}
