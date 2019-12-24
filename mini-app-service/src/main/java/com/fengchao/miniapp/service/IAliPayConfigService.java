package com.fengchao.miniapp.service;

import com.fengchao.miniapp.model.AliPayConfig;

public interface IAliPayConfigService {

    AliPayConfig getByIAppId(String iAppId);
}
