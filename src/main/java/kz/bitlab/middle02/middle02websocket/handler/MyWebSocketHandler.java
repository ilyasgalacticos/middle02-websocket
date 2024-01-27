package kz.bitlab.middle02.middle02websocket.handler;

import kz.bitlab.middle02.middle02websocket.websocket.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MyWebSocketHandler extends AbstractWebSocketHandler {

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

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
            sendMessageToAll("User : " + userName + " joined");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String request = sessions.get(session.getId()).getUsername() + " : " + message.getPayload();
        sendMessageToAll(request);
    }

    private void sendMessageToAll(String message){
        for(UserSession userSession : sessions.values()){
            try{
                userSession.getSession().sendMessage(new TextMessage(message));
            }catch (Exception e){
                System.err.println("Error on sending Websocket Message: " + message);
            }
        }
    }
}
