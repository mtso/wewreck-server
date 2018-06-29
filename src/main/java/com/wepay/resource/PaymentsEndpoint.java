package com.wepay.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ServerEndpoint("/payments")
public class PaymentsEndpoint {
    private static Logger log = LoggerFactory.getLogger(PaymentsEndpoint.class);
    private static Set<Session> sessions = new HashSet<>();
    private static Map<String, Set<Session>> merchants = new HashMap<>();
    private static ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    public void open(Session session) {
        log.info(String.format("websocket session %s opened", session.getId()));
        PaymentsEndpoint.sessions.add(session);
    }

    @OnClose
    public void close(Session session) {
        log.info(String.format("websocket session %s closed", session.getId()));
        PaymentsEndpoint.sessions.remove(session);
    }

    @OnError
    public void onError(Throwable error) {
        log.error(String.format("%s", error.getMessage()));
    }

    @OnMessage
    public void handleMessage(String message, Session session) throws IOException {
        JsonNode msgJson = mapper.readTree(message);
        String accountId = msgJson.path("account_id").asText();
        if (!merchants.containsKey(accountId)) {
            merchants.put(accountId, new HashSet<>());
        }
        merchants.get(accountId).add(session);
        session.getBasicRemote().sendText(String.format("registered %s", session.getId()));
    }

    public static void onPaymentCreated(String accountId, String message) {
        if (merchants.containsKey(accountId)) {
            Set<Session> sessions = merchants.get(accountId);
            for (Session session : sessions) {
                if (!session.isOpen()) {
                    continue;
                }
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error(e.toString());
                }
            }
        }
    }
}
