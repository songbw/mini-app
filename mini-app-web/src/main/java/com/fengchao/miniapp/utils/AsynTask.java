package com.fengchao.miniapp.utils;


import com.fengchao.miniapp.service.impl.UserInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.Future;

@Slf4j
//@Component
public class AsynTask {

    private UserInfoServiceImpl userInfoService;

    @Autowired
    public AsynTask(UserInfoServiceImpl userInfoService){
        this.userInfoService = userInfoService;
    }

    @Async
    public Future<Boolean> miniAppTask(String openId, Long recordId){
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} param : openId={} recordId={}",_func,openId,recordId);


        log.info("{} 结束",_func);
        return new AsyncResult<>(true);
    }

}
