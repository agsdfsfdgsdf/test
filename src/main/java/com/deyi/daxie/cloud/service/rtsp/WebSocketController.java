package com.deyi.daxie.cloud.service.rtsp;
import com.deyi.daxie.cloud.service.listener.CompressService;
import com.deyi.daxie.cloud.service.listener.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/*GET请求开放用于测试，最好只允许POST请求*/
@Controller
@RequestMapping(value = "/websocket/auditSocket/ws", method = {RequestMethod.POST, RequestMethod.GET})
public class WebSocketController {
    /*@Autowired
    WebSocket springWebSocketHandler;*/

    @Autowired
    private WebSocketClient springWebSocketHandler;
    @Autowired
    private Test test;


    /**
     * 登录将username放入session中，然后在拦截器HandshakeInterceptor中取出
     */
    @ResponseBody
    @RequestMapping("/login")
    public String login(HttpServletRequest request, @RequestParam(value = "messageType") String messageType, @RequestParam(value = "truckNo") String truckNo) {
        System.out.println("登录：" + messageType + "：" + truckNo);
        HttpSession session = request.getSession();
        if (null != session) {
            session.setAttribute("SESSION_USERNAME", truckNo);
            return "success";
        } else {
            return "fail";
        }
    }

    /**
     * 指定发送
     */
    @ResponseBody
    @RequestMapping("/sendToUser")
    public String send(@RequestParam String message) throws IOException {
        springWebSocketHandler.onMessage(message);
        System.out.println("发送至：" + message);
        return "success";
    }

    /**
     * 指定发送
     */
    @ResponseBody
    @RequestMapping("/send")
    public String getQdGpsAll() throws IOException {
        test.getQdGpsAll();
        return "success";
    }
}
