package com.campus.secondhand.websocket;

import com.campus.secondhand.dto.ChatMessageDTO;
import com.campus.secondhand.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final String TOKEN_PREFIX = "token:";
    private static final String ATTR_USER_ID = "currentUserId";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<Long, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ChatService chatService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        String token = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("token");
        if (token == null || token.isBlank()) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        String userInfo = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (userInfo == null || userInfo.isBlank()) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        String[] parts = userInfo.split(":");
        Long userId = Long.parseLong(parts[0]);
        session.getAttributes().put(ATTR_USER_ID, userId);
        ONLINE_USERS.put(userId, session);

        ChatMessageDTO welcome = new ChatMessageDTO();
        welcome.setType("system");
        welcome.setContent("连接成功");
        welcome.setTimestamp(System.currentTimeMillis());
        session.sendMessage(new TextMessage(MAPPER.writeValueAsString(welcome)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long fromUserId = (Long) session.getAttributes().get(ATTR_USER_ID);
        if (fromUserId == null) {
            return;
        }

        ChatMessageDTO msg = MAPPER.readValue(message.getPayload(), ChatMessageDTO.class);
        if (msg.getToUserId() == null || msg.getContent() == null || msg.getContent().isBlank()) {
            return;
        }
        msg.setFromUserId(fromUserId);
        msg.setType(msg.getType() == null || msg.getType().isBlank() ? "text" : msg.getType());
        msg.setTimestamp(System.currentTimeMillis());
        if (msg.getProductId() == null) {
            return;
        }
        chatService.saveMessage(msg);

        String payload = MAPPER.writeValueAsString(msg);

        if (session.isOpen()) {
            session.sendMessage(new TextMessage(payload));
        }

        WebSocketSession targetSession = ONLINE_USERS.get(msg.getToUserId());
        if (targetSession != null && targetSession.isOpen() && !Objects.equals(msg.getToUserId(), fromUserId)) {
            targetSession.sendMessage(new TextMessage(payload));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get(ATTR_USER_ID);
        if (userId != null) {
            ONLINE_USERS.remove(userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = (Long) session.getAttributes().get(ATTR_USER_ID);
        if (userId != null) {
            ONLINE_USERS.remove(userId);
        }
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }
}
