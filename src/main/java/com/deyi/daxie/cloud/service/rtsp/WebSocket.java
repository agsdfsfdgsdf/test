package com.deyi.daxie.cloud.service.rtsp;
import com.alibaba.fastjson.JSONObject;
import com.deyi.daxie.cloud.service.listener.LessonMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
/*import org.springframework.kafka.core.KafkaTemplate;*/
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@ServerEndpoint("/websocket/{username}") //暴露的ws应用的路径
public class WebSocket {
    public static final Map<String, Session> SESSION_MAP = new HashMap<>();
    //声明线程池，大小为100
    //private static ExecutorService executorService = Executors.newFixedThreadPool(100);
    @Autowired
    private LessonMsgService lessonMsgService;
    private static ApplicationContext applicationContext;
    /*@Autowired
    private KafkaTemplate kafkaTemplate;*/

    /**
     * 客户端与服务端连接成功
     * @param session
     * @param username
     */
    @OnOpen
    public void onOpen(Session session,@PathParam("username") String username){
        log.info(username);
        /*
            do something for onOpen
            与当前客户端连接成功时
         */
    }

    /**
     * 客户端与服务端连接关闭
     * @param session
     * @param username
     */
    @OnClose
    public void onClose(Session session,@PathParam("username") String username){
        /*
            do something for onClose
            与当前客户端连接关闭时
         */
    }

    /**
     * 客户端与服务端连接异常
     * @param error
     * @param session
     * @param username
     */
    @OnError
    public void onError(Throwable error,Session session,@PathParam("username") String username) {
    }

    /**
     * 客户端向服务端发送消息
     * @param message
     * @param username
     * @throws IOException
     */
    @OnMessage
    public void onMsg(String message,@PathParam("username") String username) throws IOException {
        //在消息发送的方法（@OnMessage）中出发线程池的执行，将消息进行异步发送
        log.info("=======",message);
        //kafkaTemplate.send("chatMessage", message);
        //executorService.execute(() -> lessonMsgService.save(message));
        /*
            do something for onMessage
            收到来自当前客户端的消息时
         */
    }

}
