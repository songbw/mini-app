package com.fengchao.miniapp.controller;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.bean.UserInfoPostBean;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.model.UserInfo;
import com.fengchao.miniapp.service.impl.UserInfoServiceImpl;
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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Validated
@Api(tags="UserInfoManager", description = "用户信息管理", produces = "application/json;charset=UTF-8")
@RestController
@RequestMapping(value = "/wechat")
@Slf4j
public class UserInfoController {

    private UserInfoServiceImpl userInfoService;

    @Autowired
    public UserInfoController(UserInfoServiceImpl userInfoService){
        this.userInfoService = userInfoService;
    }

    @ApiOperation(value = "新建UserInfo信息", notes="新建UserInfo信息")
    @PostMapping("/openinfo")
    public ResultObject<String>
    createUserInfo(HttpServletResponse response,
               @ApiParam(value="body",required=true)
               @RequestBody @Valid UserInfoPostBean data)
            throws Exception{

        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        response.setStatus(400);
        log.info(_func+" param {}", JSON.toJSONString(data));

        String openId = data.getOpenId();

        UserInfo storedUserInfo;
        try{
            storedUserInfo = userInfoService.selectByOpenId(openId);
        }catch (Exception e){
            throw new Exception(e);
        }
        if (null != storedUserInfo){
            throw new Exception(MyErrorCode.USERINFO_EXISTED + JSON.toJSONString(storedUserInfo));
        }

        UserInfo newUserInfo = new UserInfo();
        BeanUtils.copyProperties(data,newUserInfo);
        newUserInfo.setOpenId(openId);
        newUserInfo.setCreateTime(new Date());
        newUserInfo.setUpdateTime(new Date());

        long insertNum;
        try {
            insertNum = userInfoService.insert(newUserInfo);
        }catch (Exception e){
            throw new Exception(MyErrorCode.MYSQL_OPERATE_EXCEPTION+e.getMessage());
        }

        if (0 == insertNum){
            throw new Exception(MyErrorCode.MYSQL_INSERT_FAILED);
        }

        response.setStatus(201);
        log.info(_func + " success id={}",newUserInfo.getId());
        return new ResultObject<>(200,"ok",newUserInfo.getId().toString());
    }

    @ApiOperation(value = "删除UserInfo信息", notes="删除UserInfo信息")
    @DeleteMapping("/openinfo/{id}")
    public ResultObject<String>
    deleteUserInfo(HttpServletResponse response,
               @ApiParam(value="id",required=true)
               @PathVariable("id") @NotNull(message = MyErrorCode.USERINFO_ID_BLANK) Long id
               )throws Exception{

        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        log.info(_func+" param: id={}",id);

        UserInfo userInfo;
        try {
            userInfo = userInfoService.getRecordById(id);
        }catch (Exception e){
            throw new Exception(e);
        }
        if (null == userInfo){
            throw new Exception(MyErrorCode.MYSQL_SELECT_FAILED);
        }

        try{
            userInfoService.deleteById(id);
        }catch (Exception e){
            throw new Exception(e);
        }

        response.setStatus(200);
        return new ResultObject<>(200, "success",id.toString());

    }

    @ApiOperation(value = "查询UserInfo", notes="查询UserInfo")
    @GetMapping("/openinfo/list")
    public ResultObject<PageInfo<UserInfo>>
    getUserInfoList(HttpServletResponse response,
                    @ApiParam(value="pageIndex") @RequestParam(required = false) Integer pageIndex,
                    @ApiParam(value="pageSize") @RequestParam(required = false) Integer pageSize,
                    @ApiParam(value="gender") @RequestParam(required = false) Integer gender,
                    @ApiParam(value="country") @RequestParam(required = false) String country,
                    @ApiParam(value="province") @RequestParam(required = false) String province,
                    @ApiParam(value="createTimeEnd") @RequestParam(required = false) String createTimeEnd,
                    @ApiParam(value="createTimeBegin") @RequestParam(required = false) String createTimeBegin,
                    @ApiParam(value="city") @RequestParam(required = false) String city
                ) throws Exception{

        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        log.info(_func+MyErrorCode.COMMON_PARAM_SHOW
                +" pageIndex="+pageIndex
                +" pageSize="+pageSize
                +" gender="+gender
                +" country="+country
                +" province="+province
                +" city="+city
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

        PageInfo<UserInfo> pages;
        try {
            pages = userInfoService.queryList(index,pSize,"id","DESC",gender,country,province,city,timeBegin,timeEnd);
        }catch (Exception e){
            throw new Exception(MyErrorCode.MYSQL_SELECT_FAILED+e.getMessage());
        }
        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            throw new Exception(MyErrorCode.MYSQL_SELECT_NOT_FOUND);
        }

        response.setStatus(200);
        return new ResultObject<>(200, "success",pages);

    }

    @ApiOperation(value = "获取UserInfo信息", notes="获取UserInfo信息")
    @GetMapping("/openinfo")
    public ResultObject<UserInfo>
    getUserInfoByOpenId(HttpServletResponse response,
                   @ApiParam(value="openId",required=true)
                   @RequestParam @NotNull(message = MyErrorCode.OPEN_ID_BLANK) String openId
    )throws Exception{

        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        log.info(_func+" param: id={}",openId);

        UserInfo userInfo;
        try {
            userInfo = userInfoService.selectByOpenId(openId);
        }catch (Exception e){
            throw new Exception(MyErrorCode.MYSQL_OPERATE_EXCEPTION+e.getMessage());
        }
        if (null == userInfo){
            throw new Exception(MyErrorCode.MYSQL_SELECT_FAILED);
        }

        response.setStatus(200);
        return new ResultObject<>(200, "success",userInfo);

    }

}

