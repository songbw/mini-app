package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.mapper.LoginInfoMapper;
import com.fengchao.miniapp.model.LoginInfo;
import com.fengchao.miniapp.model.LoginInfoExample;
import com.fengchao.miniapp.service.ILoginInfoService;
import com.fengchao.miniapp.utils.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class LoginInfoServiceImpl implements ILoginInfoService {
    private LoginInfoMapper mapper;

    @Autowired
    public LoginInfoServiceImpl(LoginInfoMapper mapper){
        this.mapper = mapper;
    }

    @Override
    public Long
    insert(LoginInfo record)
            throws Exception{
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == record){
            String msg = functionDescription+ MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(functionDescription +MyErrorCode.COMMON_PARAM_SHOW + JSON.toJSONString(record));
        int id;
        try {
            id = mapper.insertSelective(record);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION +e.getMessage();
            log.error(functionDescription + msg);
            throw new Exception(msg);
        }

        if (0 == id){
            String msg = MyErrorCode.MYSQL_INSERT_FAILED;
            log.error(functionDescription+msg);
            throw new Exception(msg);
        }

        log.info(functionDescription + MyErrorCode.MYSQL_INSERT_SUCCESS +record.getId().toString());
        return record.getId();
    }

    @Override
    public void
    deleteById(Long id) throws Exception{
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == id || 0 == id){
            String msg = functionDescription + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(functionDescription + MyErrorCode.COMMON_PARAM_SHOW+id);
        try {
            mapper.deleteByPrimaryKey(id);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION +e.getMessage();
            log.error(functionDescription + msg);
            throw new Exception(msg);
        }
        log.info(functionDescription + MyErrorCode.MYSQL_DELETE_SUCCESS+id.toString());
    }

    @Override
    public List<LoginInfo>
    selectByOpenId(String openId) throws Exception{
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == openId || openId.isEmpty()){
            String msg = functionDescription + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info( functionDescription + MyErrorCode.COMMON_PARAM_SHOW+openId);

        LoginInfoExample example = new LoginInfoExample();
        LoginInfoExample.Criteria criteria = example.createCriteria();
        criteria.andOpenIdEqualTo(openId);

        List<LoginInfo> result;

        try {
            result = mapper.selectByExample(example);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION +e.getMessage();
            log.error(functionDescription + msg);
            throw new Exception(msg);
        }

        log.info(functionDescription + MyErrorCode.MYSQL_SELECT_SUCCESS +JSON.toJSONString(result));
        return result;
    }

    @Override
    public void
    update(LoginInfo record) throws Exception{
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == record || null == record.getId()){
            String msg = functionDescription + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(functionDescription + MyErrorCode.COMMON_PARAM_SHOW+JSON.toJSONString(record));
        try {
            mapper.updateByPrimaryKeySelective(record);
        }catch (Exception e){
            log.error(functionDescription + " mapper.updateByPrimaryKeySelective Exception {}",e.getMessage());
            throw new Exception(e);
        }
        log.info(functionDescription + " success");
    }

    @Override
    public LoginInfo
    getRecordById(Long id) throws Exception{
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == id || 0 == id){
            String msg = functionDescription + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(functionDescription+MyErrorCode.COMMON_PARAM_SHOW+id);
        LoginInfo userInfo;
        try {
            userInfo = mapper.selectByPrimaryKey(id);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION+e.getMessage();
            log.error(functionDescription+msg);
            throw new Exception(msg);
        }
        if (null == userInfo){
            log.error(functionDescription+MyErrorCode.MYSQL_SELECT_NOT_FOUND);
            throw new Exception(MyErrorCode.MYSQL_SELECT_NOT_FOUND);
        }

        log.info(functionDescription+MyErrorCode.MYSQL_SELECT_SUCCESS+JSON.toJSONString(userInfo));
        return userInfo;
    }

    @Override
    public PageInfo<LoginInfo>
    queryList(int pageIndex,  int pageSize,
              String sortKey, String orderKey,
              String openId,
              String brand,
              String model,
              String version,
              String platform,
              String system,
              Date createTimeBegin,
              Date createTimeEnd) throws Exception{

        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        LoginInfoExample example = new LoginInfoExample();
        LoginInfoExample.Criteria criteria = example.createCriteria();
        criteria.andIdIsNotNull();
        if (null != openId){
            criteria.andOpenIdEqualTo(openId);
        }
        if (null != brand){
            criteria.andBrandEqualTo(brand);
        }
        if (null != system){
            criteria.andSystemEqualTo(system);
        }
        if (null != version){
            criteria.andVersionEqualTo(version);
        }
        if (null != platform){
            criteria.andPlatformEqualTo(platform);
        }
        if (null != model){
            criteria.andModelEqualTo(model);
        }
        if(null != createTimeBegin){
            criteria.andCreateTimeGreaterThanOrEqualTo(createTimeBegin);
        }
        if (null != createTimeEnd){
            criteria.andCreateTimeLessThanOrEqualTo(createTimeEnd);
        }
        example.setOrderByClause(sortKey + " " + orderKey);

        Page pages;
        List<LoginInfo> list;
        try{
            pages = PageHelper.startPage(pageIndex, pageSize, true);
            list = mapper.selectByExample(example);
        }catch (Exception e){
            String msg=MyErrorCode.MYSQL_OPERATE_EXCEPTION+e.getMessage();
            log.error(functionDescription+msg);
            throw new Exception(msg);
        }

        return new PageInfo<>((int)pages.getTotal(), pages.getPageSize(),pageIndex,list);

    }
}
