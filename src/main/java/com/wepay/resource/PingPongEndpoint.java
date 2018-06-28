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

@ServerEndpoint("/pingpong")
public class PingPongEndpoint {
    private Logger log = LoggerFactory.getLogger(PingPongEndpoint.class);
    @OnOpen
    public void open(Session session) {
        log.info(String.format("websocket session %s opened", session.getId()));
    }

    @OnClose
    public void close(Session session) {
        log.info(String.format("websocket session %s closed", session.getId()));
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
        session.getBasicRemote().sendText("pong");
    }
}
