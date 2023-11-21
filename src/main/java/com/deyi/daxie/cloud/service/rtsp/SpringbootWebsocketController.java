package com.deyi.daxie.cloud.service.rtsp;

import com.deyi.daxie.cloud.service.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SpringbootWebsocketController {
    @Autowired
    private WebSocketClient webSocketClient;

    @RequestMapping("/get")
    public String send(){
        webSocketClient.onMessage("我是ws客户端，你好！！！");
        return "发送成功";
    }
}
