package com.fengchao.miniapp.utils;

import com.fengchao.miniapp.constant.MyErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Md5Util {

    /**
     * 密码MD5加密
     * @param str 明文
     * @return MD5
     */
    public static String md5(String str) throws Exception{
        if (log.isDebugEnabled()) {
            log.debug("try MD5 {}", str);
        }
        if(null == str){
            throw new Exception(MyErrorCode.COMMON_MD5_FAILED+"输入字符串为空");
        }
        String md5Str;
        try {
            md5Str = DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
        }catch (Exception e){
            String msg = MyErrorCode.COMMON_MD5_FAILED+e.getMessage();
            log.error("MD5 failed {}",msg,e);
            throw new Exception(msg);
        }
        return md5Str.toUpperCase();
    }
}
