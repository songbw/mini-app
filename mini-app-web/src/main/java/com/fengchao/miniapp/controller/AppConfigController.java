package com.fengchao.miniapp.controller;

import com.fengchao.miniapp.config.RenterConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags="AppConfigAPI", description = "多端接口", produces = "application/json;charset=UTF-8")
@RestController
@RequestMapping(value = "/appConfig")
@Slf4j
public class AppConfigController {

    @Autowired
    private RenterConfig renterConfig;

    @ApiOperation(value = "多端配置更新通知")
    @GetMapping("/update/notify")
    public String
    updateConfigNotify(@RequestHeader String renterId,
                       @RequestParam String appId) {

        String functionDescription = "多端配置配置更新回调 ";
        log.info("{} renterId={} appId={} ", functionDescription,renterId,appId);

        String resultOk = "success";
        renterConfig.freshAppConfig(renterId,appId);

        return resultOk;
    }
}
