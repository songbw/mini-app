package com.fengchao.miniapp.utils;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.constant.MyErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.*;

@Slf4j
public class XmlUtil {

    public static Map<String,Object>
    xml2map(String xmlStr) throws Exception{
        Map<String,Object> map = new HashMap<>();

        Document document ;
        try{
            document = DocumentHelper.parseText(xmlStr);
        }catch (Exception e){
            String msg = MyErrorCode.COMMON_XML_FAILED + e.getMessage();
            log.error("xml2map {}",msg);
            throw new Exception(msg);
        }

        if (null == document){
            String msg = MyErrorCode.COMMON_XML_FAILED + "xml 无内容";
            log.error("xml2map {}",msg);
            throw new Exception(msg);
        }

        Element root = document.getRootElement();
        if (null == root){
            String msg = MyErrorCode.COMMON_XML_FAILED + "root xml 缺失";
            log.error("xml2map {}",msg);
            throw new Exception(msg);
        }
        Iterator itr = root.elementIterator();
        while(itr.hasNext()){
            Element element = (Element)itr.next();
            if (null != element) {
                map.put(element.getName(), element.getText());
            }
        }

        log.info("解析xml 结果 {}", JSON.toJSONString(map));
        return map;
    }

    public static String map2xml(Map<String,Object> map){
        StringBuilder sb = new StringBuilder();

        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();

        sb.append("<xml>");
        while (iter.hasNext()) {
            String key = iter.next();
            if ("detail".equals(key) || "body".equals(key)
                    || "scene_info".equals(key) || "attacg".equals(key)) {

                sb.append("<");
                sb.append(key);
                sb.append("><![CDATA[");
                sb.append(map.get(key));
                sb.append("]]></");
                sb.append(key);
                sb.append(">");
            } else {

                sb.append("<");
                sb.append(key);
                sb.append(">");
                sb.append(map.get(key));
                sb.append("</");
                sb.append(key);
                sb.append(">");
            }
        }
        sb.append("</xml>");

        return sb.toString();
    }

    public static String getRandom() {
        Random random = new Random();
        String fourRandom = random.nextInt(1000000) + "";
        StringBuilder sb = new StringBuilder();
        int randLength = fourRandom.length();
        if (randLength < 6) {
            for (int i = 1; i <= 6 - randLength; i++){
                sb.append("0");
            }
        }
        sb.append(fourRandom);
        return sb.toString();
    }
}
