package com.fengchao.miniapp.controller;


import com.fengchao.miniapp.bean.WeChatTokenResultBean;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.service.impl.WeChatMiniAppClientImpl;
import com.fengchao.miniapp.utils.ResultObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Validated
@Api(tags="WeChatMiniAppApi", description = "微信小程序接口", produces = "application/json;charset=UTF-8")
@RestController
@RequestMapping(value = "/wechat")
@Slf4j
public class WeChatMiniAppController {

    private WeChatMiniAppClientImpl weChatMiniAppClient;

    @Autowired
    public WeChatMiniAppController(WeChatMiniAppClientImpl weChatMiniAppClient){

        this.weChatMiniAppClient = weChatMiniAppClient;
    }

    @ApiOperation(value = "查询UserInfo", notes="查询UserInfo")
    @GetMapping("/token")
    public ResultObject<String>
    getToken(HttpServletResponse response)throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        ResultObject<String> result = new ResultObject<>(200,"success",null);

        WeChatTokenResultBean bean;
        try{
            bean = weChatMiniAppClient.getAccessToken();
        }catch (Exception e){
            throw new Exception(e);
        }

        if (null == bean.getAccess_token()){
            throw new Exception(MyErrorCode.WECHAT_API_TOKEN_NULL);
        }

        String token = bean.getAccess_token();
        result.setData(token);
        response.setStatus(200);

        log.info(_func+"success token= "+token);
        return result;

    }
}
