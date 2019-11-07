package com.fengchao.miniapp.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JSONUtil {

    public static final String toJsonString(Object obj) {
        String json = null;

        try {
            json = JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);


        } catch (Exception e) {
            log.error("JSONUtil#toJsonString 异常:{}", e.getMessage(), e);

            json = null;
        }

        return json;
    }

    public static void main(String args[]) {
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("a", "1");
        paramMap.put("b", "2");
        paramMap.put("c", null);

        String dataJson = JSON.toJSONString(paramMap, SerializerFeature.WriteNullStringAsEmpty);
        String json = JSON.toJSONString(paramMap, SerializerFeature.WriteMapNullValue);

        System.out.println(dataJson);
        System.out.println(json);
    }
}
