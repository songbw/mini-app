package com.fengchao.miniapp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class RedisDAO {
    private static final String STORE_PREFIX = "wechatminiapp-";
    private static final String WECHAT_TOKEN_KEY = "wechattoken";

    @Autowired
    private StringRedisTemplate template;
    /*
        @Autowired
        public RedisDAO(StringRedisTemplate template){
            this.template = template;
        }
    */
    public  void setValue(String key,String value){
        if (null == key || null == value || key.isEmpty() || value.isEmpty()) {
            return;
        }
        ValueOperations<String, String> ops = template.opsForValue();
        ops.set(key,value,2, TimeUnit.HOURS);
    }

    public  void setValue(String key,String value, int expire){
        if (null == key || null == value || key.isEmpty() || value.isEmpty()) {
            return;
        }
        ValueOperations<String, String> ops = template.opsForValue();
        ops.set(key,value,expire, TimeUnit.SECONDS);
    }

    public String getValue(String key){
        if (null == key || key.isEmpty() ) {
            return null;
        }
        ValueOperations<String, String> ops = this.template.opsForValue();
        return ops.get(key);
    }

    public Boolean removeKey(String key) {
        if (null == key || key.isEmpty()) {
            return false;
        }

        return this.template.delete(key);
    }

    public void setToken(String name, String token) {
        if (null == name || null == token || name.isEmpty() || token.isEmpty()){
            return;
        }
        log.info("store token : {}",token);
        ValueOperations<String, String> ops = template.opsForValue();
        String oldToken = ops.get(STORE_PREFIX+name);
        if (null != oldToken) {
            template.delete(STORE_PREFIX + oldToken);
        }

        ops.set(STORE_PREFIX+name, token, 12*60*60,TimeUnit.SECONDS);
        ops.set(STORE_PREFIX+token,name,12*60*60,TimeUnit.SECONDS);
    }

    public void deleteToken(String name) {
        if (null == name || name.isEmpty()){
            return;
        }
        ValueOperations<String, String> ops = template.opsForValue();
        String oldToken = ops.get(STORE_PREFIX+name);
        template.delete(STORE_PREFIX+name);
        template.delete(STORE_PREFIX + oldToken);
    }

    public void storeWeChatToken(String token, Integer expire) {
        if (null == token || null == expire || token.isEmpty()){
            log.error("storeWeChatToken 入参缺失");
            return;
        }
        log.info("storeWeChatToken : {}",token);
        ValueOperations<String, String> ops = template.opsForValue();
        String oldToken = ops.get(STORE_PREFIX+WECHAT_TOKEN_KEY);
        if (null != oldToken) {
            template.delete(STORE_PREFIX+WECHAT_TOKEN_KEY);
        }

        ops.set(STORE_PREFIX+WECHAT_TOKEN_KEY, token, expire-5,TimeUnit.SECONDS);
    }
    public String getWeChatToken() {

        ValueOperations<String, String> ops = template.opsForValue();
        return ops.get(STORE_PREFIX+WECHAT_TOKEN_KEY);

    }
}

