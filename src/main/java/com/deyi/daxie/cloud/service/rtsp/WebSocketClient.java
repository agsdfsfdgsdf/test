package com.deyi.daxie.cloud.service.rtsp;

import com.alibaba.fastjson.JSONObject;
import com.deyi.daxie.cloud.service.listener.LessonMsgService;
import com.deyi.daxie.cloud.service.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
/*import org.springframework.kafka.core.KafkaTemplate;*/
import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.deyi.daxie.cloud.service.rtsp.WebSocket.SESSION_MAP;

@Slf4j
@Component
@ClientEndpoint
public class WebSocketClient {
    private static final String TCS_WEBSOCKET_SERVER = "ws://192.168.150.1:35729/websocket/{username}";
    private static ExecutorService executorService = Executors.newFixedThreadPool(30);

   // @Autowired
    //private LessonMsgService lessonMsgService;
    /*@Autowired
    private KafkaTemplate kafkaTemplate;*/
    private String truckNo;
    public Session session_cloud_tcs; // 与TCS-websocket建立连接的session
    private static LessonMsgService lessonMsgService;

    @Autowired
    public void setLessonMsgService(LessonMsgService lessonMsgService) {
        WebSocketClient.lessonMsgService = lessonMsgService;
    }
    private String username;

    public WebSocketClient initClient(String username, String token) {
        try {
            WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
            webSocketContainer.setDefaultMaxSessionIdleTimeout(0); // 无限超时
            WebSocketClient wsClient = new WebSocketClient();
            wsClient.setUsername(username);
            webSocketContainer.connectToServer(wsClient, new URI(TCS_WEBSOCKET_SERVER + "?token=" + token));
            log.info("initClient: " + TCS_WEBSOCKET_SERVER + "?token=" + token);
            return wsClient;
        } catch (Exception e) {
            log.error("WebSocketClient initClient error: " + e.getMessage());
        }
        return null;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("dell-WebSocketClient connect with TCS-websocket-server, session.getId(): "
                + session.getId() + ",truckNo: " + truckNo);
        this.session_cloud_tcs = session;
        this.session_cloud_tcs.setMaxIdleTimeout(0); // 无限超时
        this.session_cloud_tcs.setMaxTextMessageBufferSize(256 * 1024 * 1024); // 100M
        this.session_cloud_tcs.setMaxBinaryMessageBufferSize(256 * 1024 * 1024); // 100M
    }
    /*private void executeFuture(String message) {

        ExecutorService executorService = Executors.newFixedThreadPool(30);
        List<Future<String>> futureList = new LinkedList<>();
            Callable<String> callable = () -> {
                try {
                    // 保存数据代码
                    lessonMsgService.save(message);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return "Success";
            };
            Future<String> future = executorService.submit(callable);
            futureList.add(future);
    }*/

    @OnMessage
    public void onMessage(String message ) {
        log.info("++++++++message{}",message);
        /*for (int i = 0; i < 30; i++) {
            executorService.execute(new TestRunnable());
            lessonMsgService.save(message);
        }*/
        //executeFuture(message);
        /*kafkaTemplate.send("chatMessage", message);*/
        executorService.submit(() -> lessonMsgService.save(message));
        try {
            // 查找车端与TCS连接的session
            for (Map.Entry<String, Session> entry : SESSION_MAP.entrySet()) {
                String truckNo = StringUtils.split(entry.getKey(), "+")[0];
                Session session = entry.getValue();
                if (Objects.equals(this.truckNo, truckNo) && session.isOpen()) {
                    synchronized (session) {
                        session.getBasicRemote().sendText(message);
                        //log.info("TCS-WebSocketClient onMessage, send message to truck(" + truckNo + "): " + Result.toString(Result.success(messageType, tcsWsResponse)));
                    }
                }
            }
            System.out.println("ws 收到消息"+message);
        } catch (Exception e) {
            e.printStackTrace(); // 显示完整报错信息，上线后删除
            log.error("dell-WebSocketClient onMessage error: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        log.info("dell-WebSocketClient onClose, session.getId(): " + session.getId() + ", truckNo: " + truckNo);
    }

    /**
     * 断开与TCS-websocket的连接
     */
    public void closeTcsWebsocket() {
        log.info("dell-WebSocketClient closeTcsWebsocket");
        try {
            if (session_cloud_tcs.isOpen()) {
                session_cloud_tcs.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Connection closed"));
                log.info("WebSocketClient onClose executed");
            }
        } catch (IOException e) {
            log.error("closeTcsWebsocket: " + e.getMessage());
        }
    }

    /**
     * 断开连接
     *
     * @param session session
     * @param e       e
     */
    @OnError
    public void onError(Session session, Throwable e) {
        log.error("dell-WebSocketClient连接TCS-websocket-server错误, truckNo: " + this.truckNo, e);
        log.error("dell-WebSocketClient连接TCS-websocket-server错误, session.getId(): " + session.getId());
    }

    /**
     * kafka发送消息监听事件，有消息分发
     *
     * @param message
     * @author cxb
     */
    public void kafkaReceiveMsg(String message) {
        JSONObject jsonObject = JSONObject.parseObject(message);
        //String receiver_id = jsonObject.getString("receiver_id"); //接受者ID
        Session session = null;
        for (Map.Entry<String, Session> entry : SESSION_MAP.entrySet()) {
            Session value = entry.getValue();
            if (Objects.equals(session, value.getId())) {
                session = value;
                break;
            }
        }

        if (session != null) {
            session.getAsyncRemote().sendText(message);
        } else {
            log.info("Can`t find appoint id:" + session);
        }
    }
    /**
     * kafka监听关闭websocket连接
     *
     * @param closeMessage
     */
    public void kafkaCloseWebsocket(String closeMessage) {
        JSONObject jsonObject = JSONObject.parseObject(closeMessage);
        String username = jsonObject.getString("username");
        SESSION_MAP.remove(username);
    }

    public void sendMsg(String message) {
        session_cloud_tcs.getAsyncRemote().sendText(message);
    }
}

