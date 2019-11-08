package com.fengchao.miniapp.controller;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.bean.LoginInfoPostBean;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.model.LoginInfo;
import com.fengchao.miniapp.service.impl.LoginInfoServiceImpl;
import com.fengchao.miniapp.utils.DateUtil;
import com.fengchao.miniapp.utils.PageInfo;
import com.fengchao.miniapp.utils.ResultObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Date;

@Validated
@Api(tags="LoginInfoManager", description = "用户登录信息管理", produces = "application/json;charset=UTF-8")
@RestController
@RequestMapping(value = "/login")
@Slf4j
public class LoginInfoController {

    private LoginInfoServiceImpl service;

    @Autowired
    public LoginInfoController(LoginInfoServiceImpl service){

        this.service = service;

    }

    @ApiOperation(value = "新建LoginInfo信息", notes="新建LoginInfo信息")
    @PostMapping("/info")
    public ResultObject<String>
    createLoginInfo(HttpServletResponse response,
                   @ApiParam(value="body",required=true)
                   @RequestBody @Valid LoginInfoPostBean data)
            throws Exception{

        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        response.setStatus(400);
        log.info(_func+" param {}", JSON.toJSONString(data));

        String openId = data.getOpenId();

        LoginInfo newLoginInfo = new LoginInfo();
        BeanUtils.copyProperties(data,newLoginInfo);
        newLoginInfo.setOpenId(openId);
        newLoginInfo.setCreateTime(new Date());
        newLoginInfo.setUpdateTime(new Date());

        long insertNum;
        try {
            insertNum = service.insert(newLoginInfo);
        }catch (Exception e){
            throw new Exception(e);
        }

        if (0 == insertNum){
            throw new Exception(MyErrorCode.MYSQL_INSERT_FAILED);
        }

        response.setStatus(200);
        log.info(_func + " success id={}",newLoginInfo.getId());
        return new ResultObject<>(200,"ok",newLoginInfo.getId().toString());
    }

    @ApiOperation(value = "查询登录信息列表", notes="查询登录信息列表")
    @GetMapping("/info/list")
    public ResultObject<PageInfo<LoginInfo>>
    getLoginInfoList(HttpServletResponse response,
                    @ApiParam(value="pageIndex") @RequestParam(required = false) Integer pageIndex,
                    @ApiParam(value="pageSize") @RequestParam(required = false) Integer pageSize,
                    @ApiParam(value="openId") @RequestParam(required = false) String openId,
                    @ApiParam(value="brand") @RequestParam(required = false) String brand,
                    @ApiParam(value="model") @RequestParam(required = false) String model,
                    @ApiParam(value="version") @RequestParam(required = false) String version,
                    @ApiParam(value="system") @RequestParam(required = false) String system,
                    @ApiParam(value="createTimeEnd") @RequestParam(required = false) String createTimeEnd,
                    @ApiParam(value="createTimeBegin") @RequestParam(required = false) String createTimeBegin,
                    @ApiParam(value="platform") @RequestParam(required = false) String platform
                    ) throws Exception{

        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        log.info(_func+ MyErrorCode.COMMON_PARAM_SHOW
                +" pageIndex="+pageIndex
                +" pageSize="+pageSize
                +" system="+system
                +" version="+version
                +" brand="+brand
                +" model="+model
                +" platform="+platform
                +" openId="+openId
                +" createTimeBegin="+createTimeBegin
                +" createTimeEnd="+createTimeEnd
        );

        int index = (null == pageIndex || 1 > pageIndex)?1:pageIndex;
        int pSize = (null == pageSize || 1 > pageSize)?10:pageSize;

        Date timeBegin=null;
        Date timeEnd=null;

        if (null != createTimeBegin && !createTimeBegin.isEmpty()){
            timeBegin = DateUtil.String2Date(createTimeBegin);
        }
        if (null != createTimeEnd && !createTimeEnd.isEmpty()){
            timeEnd = DateUtil.String2Date(createTimeEnd);
        }

        PageInfo<LoginInfo> pages;
        try {
            pages = service.queryList(index,pSize,"id","DESC",openId,brand,model,version,platform,system,timeBegin,timeEnd);
        }catch (Exception e){
            throw new Exception(e);
        }
        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            throw new Exception(MyErrorCode.MYSQL_SELECT_NOT_FOUND);
        }

        response.setStatus(200);
        return new ResultObject<>(200, "success",pages);

    }
}
