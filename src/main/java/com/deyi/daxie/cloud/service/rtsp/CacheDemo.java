package com.deyi.daxie.cloud.service.rtsp;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheDemo {
    @RequestMapping(value = "/savePhone", method = RequestMethod.GET)
    public String  savePhone(){
        try {
            // 设置失效时间30秒
            TimeExpiredPoolCache.getInstance().put("phone","13800138000",30*1000L);
            new TimeThread().run();
            return "保存phone进入缓存成功";
        } catch (Exception e) {
            return "保存phone进入缓存失败";
        }
    }

    @RequestMapping(value = "/getPhone", method = RequestMethod.GET)
    public String  getPhone(){
        try {
            String  token = TimeExpiredPoolCache.getInstance().get("phone");
            if (null!=token){
                return token;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "数据为空................";
    }

    public static class TimeThread implements  Runnable{

        @Override
        public void run() {
            for(int i=30;i>0;i--){
                System.out.println("缓存倒计时："+i+" 秒");
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
