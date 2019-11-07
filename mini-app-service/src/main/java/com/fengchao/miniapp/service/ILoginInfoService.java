package com.fengchao.miniapp.service;

import com.fengchao.miniapp.model.LoginInfo;
import com.fengchao.miniapp.utils.PageInfo;

import java.util.Date;
import java.util.List;

public interface ILoginInfoService {
    Long insert(LoginInfo record) throws Exception;

    void deleteById(Long id) throws Exception;

    List<LoginInfo> selectByOpenId(String openId) throws Exception;

    void update(LoginInfo record) throws Exception;

    LoginInfo getRecordById(Long id) throws Exception;

    PageInfo<LoginInfo> queryList(int pageIndex,
                                  int pageSize,
                                  String sortKey,
                                  String orderKey,
                                  String openId,
                                  String brand,
                                  String model,
                                  String version,
                                  String platform,
                                  String system,
                                  Date createTimeBegin,
                                  Date createTimeEnd) throws Exception;
}
