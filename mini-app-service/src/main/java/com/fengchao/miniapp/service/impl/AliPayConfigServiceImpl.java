package com.fengchao.miniapp.service.impl;

import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.mapper.AliPayConfigMapper;
import com.fengchao.miniapp.model.AliPayConfig;
import com.fengchao.miniapp.model.AliPayConfigExample;
import com.fengchao.miniapp.service.IAliPayConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AliPayConfigServiceImpl implements IAliPayConfigService {

    private AliPayConfigMapper mapper;

    @Autowired
    public AliPayConfigServiceImpl(AliPayConfigMapper mapper){

        this.mapper = mapper;
    }

    @Override
    public AliPayConfig
    getByIAppId(String iAppId){

        if (null == iAppId){
            throw new RuntimeException(MyErrorCode.COMMON_PARAM_NULL+" iAppId is null");
        }

        AliPayConfigExample example = new AliPayConfigExample();
        AliPayConfigExample.Criteria criteria = example.createCriteria();
        criteria.andIAppIdEqualTo(iAppId);

        List<AliPayConfig> list;
        try{
            list = mapper.selectByExample(example);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new RuntimeException(MyErrorCode.MYSQL_OPERATE_EXCEPTION);
        }
        if (null == list || 0 == list.size()){
            log.error("未找到iAppId={} 的支付宝配置",iAppId);
            throw new RuntimeException(MyErrorCode.ALIPAY_CONFIG_BLANK);
        }

        return list.get(0);
    }
}
