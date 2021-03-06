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

@ServerEndpoint("/pingpong")
public class PingPongEndpoint {
    private static Logger log = LoggerFactory.getLogger(PingPongEndpoint.class);
    private static Set<Session> sessions = new HashSet<>();

    @OnOpen
    public void open(Session session) {
        log.info(String.format("websocket session %s opened", session.getId()));
        PingPongEndpoint.sessions.add(session);
    }

    @OnClose
    public void close(Session session) {
        log.info(String.format("websocket session %s closed", session.getId()));
        PingPongEndpoint.sessions.remove(session);
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

        for (Session sess: PingPongEndpoint.sessions) {
            if (sess.getId() != session.getId()) {
                sess.getBasicRemote().sendText(String.format("pong to %s", session.getId()));
            }
        }
    }
}
