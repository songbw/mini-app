package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.mapper.LoginInfoMapper;
import com.fengchao.miniapp.model.LoginInfo;
import com.fengchao.miniapp.model.LoginInfoExample;
import com.fengchao.miniapp.service.ILoginInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == record){
            String msg = _func+ MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(_func +MyErrorCode.COMMON_PARAM_SHOW + JSON.toJSONString(record));
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
    public List<LoginInfo>
    selectByOpenId(String openId) throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == openId || openId.isEmpty()){
            String msg = _func + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info( _func + MyErrorCode.COMMON_PARAM_SHOW+openId);

        LoginInfoExample example = new LoginInfoExample();
        LoginInfoExample.Criteria criteria = example.createCriteria();
        criteria.andOpenIdEqualTo(openId);

        List<LoginInfo> result;

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
    update(LoginInfo record) throws Exception{
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
    public LoginInfo
    getRecordById(Long id) throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == id || 0 == id){
            String msg = _func + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(_func+MyErrorCode.COMMON_PARAM_SHOW+id);
        LoginInfo userInfo;
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
}
