package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.mapper.RefundMapper;
import com.fengchao.miniapp.model.Refund;
import com.fengchao.miniapp.model.RefundExample;
import com.fengchao.miniapp.service.IRefundService;
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
public class RefundServiceImpl implements IRefundService {

    private RefundMapper mapper;

    @Autowired
    public RefundServiceImpl(RefundMapper mapper){
        this.mapper = mapper;
    }

    @Override
    public Long
    insert(Refund record)
            throws Exception{
        String functionDescription = "新增退款记录 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == record){
            throw new Exception(MyErrorCode.COMMON_PARAM_NULL);
        }
        log.info("{}  {}  {}",functionDescription ,MyErrorCode.COMMON_PARAM_SHOW , JSON.toJSONString(record));
        int id;
        try {
            id = mapper.insertSelective(record);
        }catch (Exception e){
            log.error("{} ",e.getMessage(),e);
            throw new Exception(MyErrorCode.MYSQL_OPERATE_EXCEPTION);
        }

        if (0 == id){
            throw new Exception(MyErrorCode.MYSQL_INSERT_FAILED);
        }

        log.info("{} {} {}",functionDescription , MyErrorCode.MYSQL_INSERT_SUCCESS ,record.getId().toString());
        return record.getId();
    }

    @Override
    public void
    deleteById(Long id) throws Exception{
        String functionDescription = "删除退款记录 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == id || 0 == id){
            throw new Exception(MyErrorCode.COMMON_PARAM_NULL);
        }
        log.info("{} {} {}",functionDescription ,MyErrorCode.COMMON_PARAM_SHOW,id);
        try {
            mapper.deleteByPrimaryKey(id);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION +e.getMessage();
            log.error("{} 异常 {}",functionDescription,e.getMessage(),e);
            throw new Exception(msg);
        }
        log.info(functionDescription + MyErrorCode.MYSQL_DELETE_SUCCESS+id.toString());
    }

    @Override
    public Refund
    selectByOpenId(String openId) throws Exception{
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == openId || openId.isEmpty()){
            throw new Exception(MyErrorCode.COMMON_PARAM_NULL);
        }
        log.info("{} {} {}", functionDescription , MyErrorCode.COMMON_PARAM_SHOW, openId);

        RefundExample example = new RefundExample();
        RefundExample.Criteria criteria = example.createCriteria();
        criteria.andOpenIdEqualTo(openId);

        List<Refund> result;

        try {
            result = mapper.selectByExample(example);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION +e.getMessage();
            log.error("{} {}",functionDescription , msg,e);
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
    update(Refund record) throws Exception{
        String functionDescription = "更新退款记录 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == record || null == record.getId()){
            throw new Exception(MyErrorCode.COMMON_PARAM_NULL);
        }
        log.info("{} {} {}",functionDescription ,MyErrorCode.COMMON_PARAM_SHOW,JSON.toJSONString(record));
        try {
            mapper.updateByPrimaryKeySelective(record);
        }catch (Exception e){
            log.error("{} mapper.updateByPrimaryKeySelective Exception {}",functionDescription,e.getMessage(),e);
            throw new Exception(e);
        }
        log.info("{} success ",functionDescription);
    }

    @Override
    public Refund
    getRecordById(Long id) throws Exception{
        String functionDescription = "根据ID获取退款记录 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == id || 0 == id){
            throw new Exception(MyErrorCode.COMMON_PARAM_NULL);
        }
        log.info("{} {}  {}",functionDescription,MyErrorCode.COMMON_PARAM_SHOW,id);
        Refund userInfo;
        try {
            userInfo = mapper.selectByPrimaryKey(id);
        }catch (Exception e){
            String msg = MyErrorCode.MYSQL_OPERATE_EXCEPTION+e.getMessage();
            log.error("{} {}",functionDescription,msg,e);
            throw new Exception(msg);
        }
        if (null == userInfo){
            throw new Exception(MyErrorCode.MYSQL_SELECT_NOT_FOUND);
        }

        log.info("{}  {}  {}",functionDescription,MyErrorCode.MYSQL_SELECT_SUCCESS,JSON.toJSONString(userInfo));
        return userInfo;
    }

    @Override
    public PageInfo<Refund> queryList(int pageIndex,
                                       int pageSize,
                                       String sortKey,
                                       String orderKey,
                                       String openId,
                                       String refundNo,
                                       String forRemoteRefundNo,
                                       String orderId,
                                       Date createTimeBegin,
                                       Date createTimeEnd)
            throws Exception{

        String functionDescription = "查询退款记录 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        RefundExample example = new RefundExample();
        RefundExample.Criteria criteria = example.createCriteria();
        criteria.andOpenIdIsNotNull();
        if (null != openId){
            criteria.andOpenIdEqualTo(openId);
        }
        if (null != orderId){
            criteria.andOrderIdEqualTo(orderId);
        }
        if (null != refundNo){
            criteria.andRefundNoEqualTo(refundNo);
        }
        if (null != forRemoteRefundNo){
            criteria.andRemoteRefundNoEqualTo(forRemoteRefundNo);
        }
        if(null != createTimeBegin){
            criteria.andCreateTimeGreaterThanOrEqualTo(createTimeBegin);
        }
        if (null != createTimeEnd){
            criteria.andCreateTimeLessThanOrEqualTo(createTimeEnd);
        }
        example.setOrderByClause(sortKey + " " + orderKey);

        Page pages;
        List<Refund> list;
        try{
            pages = PageHelper.startPage(pageIndex, pageSize, true);
            list = mapper.selectByExample(example);
        }catch (Exception e){
            String msg=MyErrorCode.MYSQL_OPERATE_EXCEPTION+e.getMessage();
            log.error("{} {}",functionDescription,msg,e);
            throw new Exception(msg);
        }

        return new PageInfo<>((int)pages.getTotal(), pages.getPageSize(),pageIndex,list);

    }
}
