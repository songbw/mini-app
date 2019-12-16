package com.fengchao.miniapp.config;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.bean.AllConfigurationBean;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 所需的配置信息从miniApp-*.yml中获取
 */
@Configuration
@ConfigurationProperties("weixin")
@Component
@Slf4j
@Getter
@Setter
public class MiniAppConfiguration {

    List<AllConfigurationBean> ids;

    @PostConstruct//在servlet初始化的时候加载，并且只加载一次，和构造代码块的作用类似
    private void init() throws Exception{
        log.info("load miniApp properties start!");

        if (null == ids || 0 == ids.size()){
            String msg = "configuration of miniAppId not found";
            log.error(msg);
            throw new Exception(msg);
        }else {
            log.info("miniApp configuration array size={}", ids.size());
        }

        log.info("miniApp configuration = {}", JSON.toJSONString(ids));

    }


    public AllConfigurationBean getConfig(String iAppId) {
        String _func = "获取配置项: ";
        if (null == iAppId || iAppId.isEmpty()){
            log.error("{} iAppId 缺失",_func);
            return null;
        }


        if (null == this.ids || 0 == this.ids.size()){
            log.error("{} 没有发现任何配置项",_func);
            return null;
        }

        for (AllConfigurationBean b: ids){
            if (b.getIAppId().equals(iAppId)){
                log.info("{} {}",_func,JSON.toJSONString(b));
                return b;
            }
        }

        log.error("{} 没有发现iAppId={} 的配置项",_func,iAppId);
        return null;
    }
}
