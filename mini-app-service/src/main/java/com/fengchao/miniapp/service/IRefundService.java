package com.fengchao.miniapp.service;

import com.fengchao.miniapp.model.Refund;
import com.fengchao.miniapp.utils.PageInfo;

import java.util.Date;

public interface IRefundService {

    Long insert(Refund record) throws Exception;

    void deleteById(Long id) throws Exception;

    Refund selectByOpenId(String openId) throws Exception;

    void update(Refund record) throws Exception;

    Refund getRecordById(Long id) throws Exception;

    PageInfo<Refund> queryList(int pageIndex,
                                int pageSize,
                                String sortKey,
                                String orderKey,
                                String openId,
                                String refundNo,
                                String forWxRefundNo,
                                String orderId,
                                Date createTimeBegin,
                                Date createTimeEnd) throws Exception;
}
