package com.fengchao.miniapp.service;

import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.utils.PageInfo;

import java.util.Date;

public interface IPaymentService {
    Long insert(Payment record) throws Exception;

    void deleteById(Long id) throws Exception;

    Payment selectByOpenId(String openId) throws Exception;

    void update(Payment record) throws Exception;

    Payment getRecordById(Long id) throws Exception;

    PageInfo<Payment> queryList(int pageIndex,
                                 int pageSize,
                                 String sortKey,
                                 String orderKey,
                                 String openId,
                                 String orderId,
                                 Date createTimeBegin,
                                 Date createTimeEnd) throws Exception;
}
