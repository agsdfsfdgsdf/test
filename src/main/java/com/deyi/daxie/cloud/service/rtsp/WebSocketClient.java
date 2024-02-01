package com.deyi.daxie.cloud.service.rtsp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.deyi.daxie.cloud.service.bean.MessageType;
import com.deyi.daxie.cloud.service.listener.LessonMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
/*import org.springframework.kafka.core.KafkaTemplate;*/
import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.*;

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

    @OnMessage
    public void onMessage(String message ) {
        log.info("++++++++message{}",message);
        executorService.submit(() -> lessonMsgService.save(message));
        JSONObject obj = JSON.parseObject(message);
        MessageType type = MessageType.getByValue(obj.getString("msg"));
        System.out.println(type);
        String truckNo = "T904";
        StringProcessor processor = new StringProcessor();
        try {
            processor.processString(truckNo, type.toString(), message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            // 查找车端与TCS连接的session
            for (Map.Entry<String, Session> entry : SESSION_MAP.entrySet()) {
                //String truckNo = StringUtils.split(entry.getKey(), "+")[0];
                Session session = entry.getValue();
                if (Objects.equals(this.truckNo, truckNo) && session.isOpen()) {
                    synchronized (session) {
                        //processString(truckNo, String.valueOf(type), message);
                        //Map<String,Map<String,String>> map = matchlist(session,type,message);
                        /*String decompressedMessage = decompress(message);
                        System.out.println(decompressedMessage);*/
                        session.getBasicRemote().sendText(message);
                        //log.info("TCS-WebSocketClient onMessage, send message to truck(" + truckNo + "): " + Result.toString(Result.success(messageType, tcsWsResponse)));
                    }
                }
            }
            /*log.info("______===={}",message);
            String zip = zip(message);
            log.info("______{}",zip);
            unzip(zip);
            System.out.println("ws 收到消息===="+unzip(zip));*/
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

   /* private String decompress(String compressedMessage) {
        try {
            byte[] compressedData = compressedMessage.getBytes();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            GZIPInputStream gis = new GZIPInputStream(bis);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
            gis.close();
            return bos.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }*/
   private String decompress(String compressedMessage) {
       if (compressedMessage == null || compressedMessage.length() == 0) {
           return compressedMessage;
       }
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       GZIPOutputStream gzip = null;
       try {
           gzip = new GZIPOutputStream(out);
           gzip.write(compressedMessage.getBytes());
       } catch (IOException e) {
           e.printStackTrace();
       } finally {
           if (gzip != null) {
               try {
                   gzip.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }
       return new sun.misc.BASE64Encoder().encode(out.toByteArray());
   }
    /**
     * 使用gzip解压缩
     * @param compressedStr 压缩字符串
     * @return
     */
    public  String uncompress(String compressedStr) {
        if (compressedStr == null) {
            return null;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = null;
        GZIPInputStream ginzip = null;
        byte[] compressed = null;
        String decompressed = null;
        try {
            compressed = new sun.misc.BASE64Decoder().decodeBuffer(compressedStr);
            in = new ByteArrayInputStream(compressed);
            ginzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ginzip != null) {
                try {
                    ginzip.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return decompressed;
    }

    public static final String zip(String str) {
        if (str == null)
            return null;
        byte[] compressed;
        ByteArrayOutputStream out = null;
        ZipOutputStream zout = null;
        String compressedStr = null;
        try {
            out = new ByteArrayOutputStream();
            zout = new ZipOutputStream(out);
            zout.putNextEntry(new ZipEntry("0"));
            zout.write(str.getBytes());
            zout.closeEntry();
            compressed = out.toByteArray();
            compressedStr = new sun.misc.BASE64Encoder().encodeBuffer(compressed);
        } catch (IOException e) {
            compressed = null;
        } finally {
            if (zout != null) {
                try {
                    zout.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return compressedStr;
    }

    /**
     * 使用zip进行解压缩
     *
     * @param compressedStr 压缩后的文本
     * @return 解压后的字符串
     */
    public static final String unzip(String compressedStr) {
        if (compressedStr == null) {
            return null;
        }


        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        ZipInputStream zin = null;
        String decompressed = null;
        try {
            byte[] compressed = new sun.misc.BASE64Decoder().decodeBuffer(compressedStr);
            out = new ByteArrayOutputStream();
            in = new ByteArrayInputStream(compressed);
            zin = new ZipInputStream(in);
            zin.getNextEntry();
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = zin.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString();
        } catch (IOException e) {
            decompressed = null;
        } finally {
            if (zin != null) {
                try {
                    zin.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return decompressed;
    }
   /* public Map<String, LinkedHashMap<String, String>> matchlist(Map<String, LinkedHashMap<String, String>> map1, String message, String type, String truckNo) {
        if(map1==null){
            Map<String, TreeMap<String, String>> msg = new HashMap<String, TreeMap<String, String>>();
            Map<String, String> mapSub = new TreeMap<>();
            mapSub.put(type,message);
            map1.put(truckNo, (LinkedHashMap<String, String>) mapSub);
            return map1;
        }
        Map<String, Map<String, String>> msg = new LinkedHashMap<String, Map<String, String>>();
        Map<String, String> map2 = new TreeMap<>();
        map2.put(type,message);
        msg.put(truckNo,map2);
        for (Map.Entry<String, LinkedHashMap<String, String>> entry : map1.entrySet()) {
            String deviceNum = entry.getKey();
            Map<String, String> mapType = entry.getValue();
            for (Map.Entry<String,Map<String, String>> entry1 : msg.entrySet()) {
                String deviceNum1 = entry1.getKey();
                LinkedHashMap<String, String> mapType1 = entry.getValue();
                if (deviceNum.equals(deviceNum1)) {
                    String messageType = "";
                    String messageType1 = "";
                    String data = "";
                    String data1 = "";
                    for (Map.Entry<String, String> mapString : mapType.entrySet()) {
                        messageType = mapString. getKey();
                        data = entry.getValue().toString();
                    }
                    for (Map.Entry<String, String> mapString1 : mapType1.entrySet()) {
                        messageType1 = entry.getKey();
                        data1 = entry.getValue().toString();
                    }
                    if (messageType1.equals(messageType) && data1.equals(data)) {
                        map1.remove(truckNo);
                        continue;
                    } else {
                        mapType1.remove(mapType1);
                        map1.put(truckNo, (LinkedHashMap<String, String>) map2);
                    }
                } else {
                    mapType1.remove(mapType1);
                    map1.put(truckNo, (LinkedHashMap<String, String>) map2);
                }
            }

        }
        return map1;
    }*/
}

