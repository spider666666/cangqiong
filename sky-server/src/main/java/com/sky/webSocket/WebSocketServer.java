package com.sky.webSocket;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {


    //1.存放会话对象(根据不同的id存储不同的会话信息)
    private static final Map<String, Session> sessionMap = new HashMap<>();

    //成功连接自动回调的方法
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid){
        System.out.println("客户端建立连接:"+sid);
//        保存连接信息
        sessionMap.put(sid, session);

    }
    //成功接收客户端信息自动回调的方法
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid){
        System.out.println("接受信息为:"+message);

    }

    //成功关闭连接自动回调的方法
    @OnClose
    public void onClose(@PathParam("sid") String sid){
        System.out.println("已经终止连接");
        sessionMap.remove(sid);

    }
    // 新增：处理连接错误的回调方法
    @OnError
    public void onError(Session session, Throwable error, @PathParam("sid") String sid) {
        System.err.println("客户端["+sid+"]连接发生错误");
        error.printStackTrace();
    }
    public void sendToAllClient(String Message){
        Collection<Session> values = sessionMap.values();
        for (Session session:values){
            try {
                session.getBasicRemote().sendText(Message);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
