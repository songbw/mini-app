package com.fengchao.miniapp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@ResponseBody
public class MyExceptionHandler {

    private static final String CODE = "code";
    private static final String MESSAGE = "msg";
    private static final String DATA = "data";

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Map<String, Object> errorHandle(Exception e, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();
        log.error(e.getMessage());
        String message = e.getMessage();
        String errData = null;
        String errMessage = message.trim();
        int errCode = 400;
        if (null != message){
            String defMsg = "default message";
            int msgIndex = message.lastIndexOf(defMsg);
            if (0 < msgIndex){
                errMessage = message.substring(msgIndex+defMsg.length()).trim();
            }else {
                if (message.contains(":")) {
                    String[] errInfo = message.split(":");
                    errMessage = errInfo[errInfo.length - 1].trim();
                }
            }

            int codeIndexBegin = errMessage.indexOf("@");
            int codeIndexEnd = errMessage.lastIndexOf("@");
            if (0 <= codeIndexBegin && 1 < codeIndexEnd){
                String codeStr = errMessage.substring(codeIndexBegin+1,codeIndexEnd);
                Integer code;
                try {
                    code = Integer.valueOf(codeStr);
                }catch (Exception ex){
                    log.error("wrong error code {}",codeStr);
                    code = 0;
                }
                if (0 < code){
                    errMessage = errMessage.substring(codeIndexEnd+1);
                    errCode = code;
                }
            }
        }

        if (null != e.getCause()) {
            errData = e.getCause().getMessage();
        }
        map.put(MESSAGE,errMessage);
        map.put(CODE,errCode);
        map.put(DATA,errData);

        response.setStatus(400);
        return map;
    }
}