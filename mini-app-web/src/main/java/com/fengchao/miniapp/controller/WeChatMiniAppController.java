package com.fengchao.miniapp.controller;


import com.fengchao.miniapp.bean.WeChatSessionResultBean;
import com.fengchao.miniapp.bean.WeChatTokenResultBean;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.model.UserInfo;
import com.fengchao.miniapp.service.impl.UserInfoServiceImpl;
import com.fengchao.miniapp.service.impl.WeChatMiniAppClientImpl;
import com.fengchao.miniapp.utils.RedisDAO;
import com.fengchao.miniapp.utils.ResultObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Validated
@Api(tags="WeChatMiniAppApi", description = "微信小程序接口", produces = "application/json;charset=UTF-8")
@RestController
@RequestMapping(value = "/wechat")
@Slf4j
public class WeChatMiniAppController {

    private WeChatMiniAppClientImpl weChatMiniAppClient;
    private RedisDAO redisDAO;
    private UserInfoServiceImpl userInfoService;

    @Autowired
    public WeChatMiniAppController(UserInfoServiceImpl userInfoServic,RedisDAO redisDAO,WeChatMiniAppClientImpl weChatMiniAppClient){

        this.weChatMiniAppClient = weChatMiniAppClient;
        this.redisDAO = redisDAO;
        this.userInfoService = userInfoServic;

    }

    @ApiOperation(value = "查询UserInfo", notes="查询UserInfo")
    @GetMapping("/token")
    public ResultObject<String>
    getToken(HttpServletResponse response)throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        ResultObject<String> result = new ResultObject<>(200,"success",null);

        String storedToken = redisDAO.getWeChatToken();
        if (null != storedToken){
            log.info(_func+" ==got stored token "+storedToken);
            result.setData(storedToken);
            response.setStatus(200);
            return result;
        }

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
        redisDAO.storeWeChatToken(token,bean.getExpires_in());
        log.info(_func+"success token= "+token);
        return result;

    }

    @ApiOperation(value = "获取session", notes="获取session")
    @GetMapping("/login")
    public ResultObject<WeChatSessionResultBean>
    getSession(HttpServletResponse response,
               @RequestParam  @Valid @NotBlank(message=MyErrorCode.WECHAT_API_JS_CODE_BLANK) String jsCode)
            throws Exception{

        ResultObject<WeChatSessionResultBean> result = new ResultObject<>(200,"success",null);

        WeChatSessionResultBean bean;
        try{
            bean = weChatMiniAppClient.getSession(jsCode);
        }catch (Exception e){
            throw e;
        }

        UserInfo info = null;
        try{
            info = userInfoService.selectByOpenId(bean.getOpenid());
        }catch (Exception e){
            throw e;
        }

        bean.setIsNewUser((null == info)?"0":"1");
        result.setData(bean);
        response.setStatus(200);

        return result;

    }

}
