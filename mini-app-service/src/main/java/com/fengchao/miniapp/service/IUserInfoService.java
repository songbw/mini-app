package com.fengchao.miniapp.service;

import com.fengchao.miniapp.model.UserInfo;
import com.fengchao.miniapp.utils.PageInfo;

import java.util.Date;
import java.util.List;

public interface IUserInfoService {
    Long insert(UserInfo record) throws Exception;

    void deleteById(Long id) throws Exception;

    UserInfo selectByOpenId(String openId) throws Exception;

    void update(UserInfo record) throws Exception;

    UserInfo getRecordById(Long id) throws Exception;

    PageInfo<UserInfo> queryList(int pageIndex,
                                 int pageSize,
                                 String sortKey,
                                 String orderKey,
                                 Integer gender,
                                 String country,
                                 String provence,
                                 String city,
                                 Date createTimeBegin,
                                 Date createTimeEnd) throws Exception;

}
