package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.mapper.PaymentMapper;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.PaymentExample;
import com.fengchao.miniapp.service.IPaymentService;
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
public class PaymentServiceImpl implements IPaymentService {

    private PaymentMapper mapper;

    @Autowired
    public PaymentServiceImpl(PaymentMapper mapper){
        this.mapper = mapper;
    }

    @Override
    public Long
    insert(Payment record)
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
            log.error("{} {}",functionDescription,e.getMessage(),e);
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
    public Payment
    selectByOpenId(String openId) throws Exception{
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == openId || openId.isEmpty()){
            String msg = functionDescription + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info( functionDescription + MyErrorCode.COMMON_PARAM_SHOW+openId);

        PaymentExample example = new PaymentExample();
        PaymentExample.Criteria criteria = example.createCriteria();
        criteria.andOpenIdEqualTo(openId);

        List<Payment> result;

        try {
            result = mapper.selectByExample(example);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION +e.getMessage();
            log.error(functionDescription + msg);
            throw new Exception(msg);
        }

        log.info(functionDescription + MyErrorCode.MYSQL_SELECT_SUCCESS +JSON.toJSONString(result));
        if (0 < result.size()) {
            return result.get(0);
        }else {
            return null;
        }
    }

    @Override
    public void
    update(Payment record) throws Exception{
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
    public Payment
    getRecordById(Long id) throws Exception{
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == id || 0 == id){
            String msg = functionDescription + MyErrorCode.COMMON_PARAM_NULL;
            log.error(msg);
            throw new Exception(msg);
        }
        log.info(functionDescription+MyErrorCode.COMMON_PARAM_SHOW+id);
        Payment userInfo;
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
    public PageInfo<Payment> queryList(int pageIndex,
                                        int pageSize,
                                        String sortKey,
                                        String orderKey,
                                        String openId,
                                        String orderId,
                                        Date createTimeBegin,
                                        Date createTimeEnd)
            {

        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        PaymentExample example = new PaymentExample();
        PaymentExample.Criteria criteria = example.createCriteria();
        criteria.andOpenIdIsNotNull();
        if (null != openId){
            criteria.andOpenIdEqualTo(openId);
        }
        if (null != orderId){
            criteria.andOrderIdEqualTo(orderId);
        }

        if(null != createTimeBegin){
            criteria.andCreateTimeGreaterThanOrEqualTo(createTimeBegin);
        }
        if (null != createTimeEnd){
            criteria.andCreateTimeLessThanOrEqualTo(createTimeEnd);
        }
        example.setOrderByClause(sortKey + " " + orderKey);

        Page pages;
        List<Payment> list;
        try{
            pages = PageHelper.startPage(pageIndex, pageSize, true);
            list = mapper.selectByExample(example);
        }catch (Exception e){
            String msg=MyErrorCode.MYSQL_OPERATE_EXCEPTION+ "payment";
            log.error("{} {}",functionDescription,e.getMessage(),e);
            throw new RuntimeException(msg);
        }

        return new PageInfo<>((int)pages.getTotal(), pages.getPageSize(),pageIndex,list);

    }
}
