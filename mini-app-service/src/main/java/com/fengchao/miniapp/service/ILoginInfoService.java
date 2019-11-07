package com.fengchao.miniapp.service;

import com.fengchao.miniapp.model.LoginInfo;

import java.util.List;

public interface ILoginInfoService {
    Long insert(LoginInfo record) throws Exception;

    void deleteById(Long id) throws Exception;

    List<LoginInfo> selectByOpenId(String openId) throws Exception;

    void update(LoginInfo record) throws Exception;

    LoginInfo getRecordById(Long id) throws Exception;
}
