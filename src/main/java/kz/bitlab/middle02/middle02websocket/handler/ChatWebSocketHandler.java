package kz.bitlab.middle02.middle02websocket.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.bitlab.middle02.middle02websocket.message.ChatCustomMessage;
import kz.bitlab.middle02.middle02websocket.service.ChatMessageService;
import kz.bitlab.middle02.middle02websocket.websocket.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends AbstractWebSocketHandler {

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> usernameToSessionIdMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ChatMessageService chatMessageService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userName = null;
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] queryParams = query.split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=");
                if (keyValue[0].equals("username") && keyValue.length > 1) {
                    userName = keyValue[1];
                    break;
                }
            }
        }

        if (userName != null) {
            sessions.put(session.getId(), new UserSession(session, userName));
            usernameToSessionIdMap.put(userName, session.getId());
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        ChatCustomMessage chatCustomMessage = parseMessage(payload);

        if (chatCustomMessage != null) {

            handleChatMessage(session, chatCustomMessage);

        }
    }

    private void handleChatMessage(WebSocketSession session, ChatCustomMessage message) {

        String sender = sessions.get(session.getId()).getUsername();
        String receiver = message.getReceiver();

        chatMessageService.saveMessage(sender, receiver, message.getContent());

        String sessionId = usernameToSessionIdMap.get(receiver);
        if (sessionId != null) {
            UserSession userSession = sessions.get(sessionId);
            if(userSession!=null){
                try{
                    userSession.getSession().sendMessage(new TextMessage(sender + " : " + message.getContent()));
                }catch (IOException e){
                    System.err.println("Error on sending WebSocket message " + e.getMessage());
                }
            }
        }

    }

    private ChatCustomMessage parseMessage(String payload) {
        try {
            return objectMapper.readValue(payload, ChatCustomMessage.class);
        } catch (JsonProcessingException e) {
            log.info("Error on parsing message " + e.getMessage());
            return null;
        }
    }
}
