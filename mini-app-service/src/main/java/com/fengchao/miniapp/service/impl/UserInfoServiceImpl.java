package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.mapper.UserInfoMapper;
import com.fengchao.miniapp.model.UserInfo;
import com.fengchao.miniapp.model.UserInfoExample;
import com.fengchao.miniapp.service.IUserInfoService;
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
public class UserInfoServiceImpl implements IUserInfoService {

    private UserInfoMapper mapper;

    @Autowired
    public UserInfoServiceImpl(UserInfoMapper mapper){
        this.mapper = mapper;
    }

    @Override
    public Long
    insert(UserInfo record)
            throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == record){
            String msg = _func+ MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(_func +MyErrorCode.COMMON_PARAM_SHOW +JSON.toJSONString(record));
        int id;
        try {
            id = mapper.insertSelective(record);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION +e.getMessage();
            log.error(_func + msg);
            throw new Exception(msg);
        }

        if (0 == id){
            String msg = MyErrorCode.MYSQL_INSERT_FAILED;
            log.error(_func+msg);
            throw new Exception(msg);
        }

        log.info(_func + MyErrorCode.MYSQL_INSERT_SUCCESS +record.getId().toString());
        return record.getId();
    }

    @Override
    public void
    deleteById(Long id) throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == id || 0 == id){
            String msg = _func + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(_func + MyErrorCode.COMMON_PARAM_SHOW+id);
        try {
            mapper.deleteByPrimaryKey(id);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION +e.getMessage();
            log.error(_func + msg);
            throw new Exception(msg);
        }
        log.info(_func + MyErrorCode.MYSQL_DELETE_SUCCESS+id.toString());
    }

    @Override
    public List<UserInfo>
    selectByOpenId(String openId) throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == openId || openId.isEmpty()){
            String msg = _func + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info( _func + MyErrorCode.COMMON_PARAM_SHOW+openId);

        UserInfoExample example = new UserInfoExample();
        UserInfoExample.Criteria criteria = example.createCriteria();
        criteria.andOpenIdEqualTo(openId);

        List<UserInfo> result;

        try {
            result = mapper.selectByExample(example);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION +e.getMessage();
            log.error(_func + msg);
            throw new Exception(msg);
        }

        log.info(_func + MyErrorCode.MYSQL_SELECT_SUCCESS +JSON.toJSONString(result));
        return result;
    }

    @Override
    public void
    update(UserInfo record) throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == record || null == record.getId()){
            String msg = _func + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(_func + MyErrorCode.COMMON_PARAM_SHOW+JSON.toJSONString(record));
        try {
            mapper.updateByPrimaryKeySelective(record);
        }catch (Exception e){
            log.error(_func + " mapper.updateByPrimaryKeySelective Exception {}",e.getMessage());
            throw new Exception(e);
        }
        log.info(_func + " success");
    }

    @Override
    public UserInfo
    getRecordById(Long id) throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == id || 0 == id){
            String msg = _func + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(_func+MyErrorCode.COMMON_PARAM_SHOW+id);
        UserInfo userInfo;
        try {
            userInfo = mapper.selectByPrimaryKey(id);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION+e.getMessage();
            log.error(_func+msg);
            throw new Exception(msg);
        }
        if (null == userInfo){
            log.error(_func+MyErrorCode.MYSQL_SELECT_NOT_FOUND);
            throw new Exception(MyErrorCode.MYSQL_SELECT_NOT_FOUND);
        }

        log.info(_func+MyErrorCode.MYSQL_SELECT_SUCCESS+JSON.toJSONString(userInfo));
        return userInfo;
    }

    @Override
    public PageInfo<UserInfo> queryList(int pageIndex,
                                        int pageSize,
                                        String sortKey,
                                        String orderKey,
                                        Integer gender,
                                        String country,
                                        String province,
                                        String city,
                                        Date createTimeBegin,
                                        Date createTimeEnd)
            throws Exception{

        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        UserInfoExample example = new UserInfoExample();
        UserInfoExample.Criteria criteria = example.createCriteria();
        criteria.andOpenIdIsNotNull();
        if (null != gender){
            criteria.andGenderEqualTo(gender);
        }
        if (null != country){
            criteria.andCountryEqualTo(country);
        }
        if (null != province){
            criteria.andProvinceEqualTo(province);
        }
        if (null != city){
            criteria.andCityEqualTo(city);
        }
        if(null != createTimeBegin){
            criteria.andCreateTimeGreaterThanOrEqualTo(createTimeBegin);
        }
        if (null != createTimeEnd){
            criteria.andCreateTimeLessThanOrEqualTo(createTimeEnd);
        }
        example.setOrderByClause(sortKey + " " + orderKey);

        Page pages;
        List<UserInfo> list;
        try{
            pages = PageHelper.startPage(pageIndex, pageSize, true);
            list = mapper.selectByExample(example);
        }catch (Exception e){
            String msg=MyErrorCode.MYSQL_OPERATE_EXCEPTION+e.getMessage();
            log.error(_func+msg);
            throw new Exception(msg);
        }

        return new PageInfo<>((int)pages.getTotal(), pages.getPageSize(),pageIndex,list);

    }
}