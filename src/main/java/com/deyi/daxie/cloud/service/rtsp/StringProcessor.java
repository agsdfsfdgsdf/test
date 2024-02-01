package com.deyi.daxie.cloud.service.rtsp;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class StringProcessor {
    /**
     * 每个缓存生效时间12小时
     */
    public static final long CACHE_HOLD_TIME_12H = 10 * 60 * 60 * 1000L;
    /*private Map<String, Map<String, String>> stringMap;

    public StringProcessor() {
        stringMap = new LinkedHashMap<>();
    }

    public void processString(String session, String type, String message) {
        log.info("^^^^{}",stringMap);
        if(stringMap==null){
            Map<String, String> sessionMap = new LinkedHashMap<>();
            sessionMap.put(type,message);
            stringMap.put(session, sessionMap);
        }
        if (!stringMap.containsKey(session)) {
            stringMap.put(session, new LinkedHashMap<>());
        }

        Map<String, String> sessionMap = stringMap.get(session);

        if (message.isEmpty()) {
            sessionMap.put(type, message);
        } else {
            String lastValue = sessionMap.get(type);

            if (lastValue == null || !lastValue.equals(message)) {
                sessionMap.put(type, message);
            } else {
                // 删除Session和类型一样的最后一条信息的字符串
                sessionMap.remove(type);
            }
        }
    }*/

    /*public static void main(String[] args) {
        StringProcessor processor = new StringProcessor();

        processor.processString("sess1", "type1", "Hello");
        System.out.println(processor.stringMap); // Output: {sess1={type1=Hello}}

        processor.processString("sess2", "type2", "");
        System.out.println(processor.stringMap); // Output: {sess1={type1=Hello}, sess2={type2=}}

        processor.processString("sess1", "type1", "World");
        System.out.println(processor.stringMap); // Output: {sess1={type1=World}, sess2={type2=}}

        processor.processString("sess3", "type1", "Hello");
        System.out.println(processor.stringMap); // Output: {sess1={type1=World}, sess2={type2=}, sess3={type1=Hello}}

        processor.processString("sess1", "type2", "Hello");
        System.out.println(processor.stringMap); // Output: {sess1={type2=Hello}, sess2={type2=}, sess3={type1=Hello}}
    }*/
    private Map<String, LinkedHashMap<String, String>> stringMap;

    public StringProcessor() {
        stringMap = new HashMap<>();
    }

    public String processString(String session, String type, String message) throws Exception {
        Map<String, String> sessionMap = new LinkedHashMap<>();
        if (message.isEmpty()) {
            return null;
        }
        LinkedHashMap<String, String> lastMap;
        try {
            stringMap = TimeExpiredPoolCache.getInstance().get(session);
            log.info("&&&&======{}", stringMap);
            if (stringMap == null) {
                log.info("^^^^{}", type);
                sessionMap.put(type, message);
                try {
                    TimeExpiredPoolCache.getInstance().put(session, sessionMap, CACHE_HOLD_TIME_12H);
                    log.info("&&&&{}", (Object) TimeExpiredPoolCache.getInstance().get(session));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return message;
            } else {
               // lastMap = stringMap.get(session);
                //log.info("lastMap{}", lastMap);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!stringMap.containsKey(type)) {
            sessionMap.put(type, message);
            TimeExpiredPoolCache.getInstance().put(session, sessionMap, CACHE_HOLD_TIME_12H);
            log.info("&&&&!!!!{}", sessionMap.get(type));
        } else {
            Iterator<Map.Entry<String, LinkedHashMap<String, String>>> iterator = stringMap.entrySet().iterator();
            Map.Entry<String, LinkedHashMap<String, String>> lastElement = null;
            while (iterator.hasNext()) {
                lastElement = iterator.next();
                log.info("&&&&****{}", lastElement);
            }
            if (lastElement == null) {
                sessionMap.put(type, message);
                TimeExpiredPoolCache.getInstance().put(session, sessionMap, CACHE_HOLD_TIME_12H);
                log.info("&&&&@@@@@{}", sessionMap.get(type));
            } else {
                if (!lastElement.getKey().equals(type)) {
                    sessionMap.put(type, message);
                    TimeExpiredPoolCache.getInstance().put(session, sessionMap, CACHE_HOLD_TIME_12H);
                    log.info("&&&&$$$${}", sessionMap.get(type));
                } else {
                    String msg = String.valueOf(lastElement.getValue());
                    log.info("&&&&9999{}", msg);
                    if (!msg.equals(message)) {
                        sessionMap.remove(lastElement.getKey());
                        sessionMap.put(type, message);
                        TimeExpiredPoolCache.getInstance().put(session, sessionMap, CACHE_HOLD_TIME_12H);
                        log.info("&&&&%%%%{}", sessionMap.get(type));
                    } else {
                        // 删除Session和类型一样的最后一条信息的字符串
                        sessionMap.remove(lastElement.getKey());
                        return null;
                    }
                }
            }
        }
        return message;
    }
}
