package com.campus.secondhand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.secondhand.common.Result;
import com.campus.secondhand.dto.ChatMessageDTO;
import com.campus.secondhand.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/history")
    public Result<IPage<ChatMessageDTO>> history(@RequestParam Long toUserId,
                                                 @RequestParam Long productId,
                                                 @RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "20") Integer size,
                                                 HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        return Result.success(chatService.listHistory(currentUserId, toUserId, productId, current, size));
    }
}
