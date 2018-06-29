package com.wepay.resource;

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
    private static Map<String, Session> merchants = new HashMap<>();

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
        if (!message.equals("ping")) {
            throw new IllegalArgumentException("Invalid message received: " + message);
        }
        session.getBasicRemote().sendText(String.format("pong to %s", session.getId()));

        for (Session sess: PaymentsEndpoint.sessions) {
            if (sess.getId() != session.getId()) {
                sess.getBasicRemote().sendText(String.format("pong to %s", session.getId()));
            }
        }
    }

    public static void onPaymentCreated(String merchantId, String message) {
        if (merchants.containsKey(merchantId)) {
            Session session = merchants.get(merchantId);
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error(e.toString());
            }
        }
    }
}
