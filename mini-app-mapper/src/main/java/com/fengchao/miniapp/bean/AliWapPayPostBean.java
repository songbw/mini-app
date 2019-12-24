package com.fengchao.miniapp.bean;

import com.fengchao.miniapp.constant.MyErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ApiModel(value="外部商户创建订单并支付Post Bean")
public class AliWapPayPostBean {

    @ApiModelProperty(value = "iAppId", example = "01")
    private String iAppId;

    @ApiModelProperty(value = "商品的标题/交易标题/订单标题/订单关键字等", example = "大乐透")
    private String subject;

    @ApiModelProperty(value = "商户网站唯一订单号", example = "1120190001")
    private String tradeNo;

    @ApiModelProperty(value = "订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]", example = "9.00")
    private Float totalAmount;

    public void verifyValue(){
        String beanDescription = "支付宝创建订单并支付参数";
        String errMsg = MyErrorCode.ALIPAY_PAYMENT_POST_PARAM_BLANK;
        if (null == iAppId || iAppId.isEmpty()){
            errMsg += " iAppId";
            log.error("{} {}",beanDescription,errMsg);
            throw new RuntimeException(errMsg);
        }
        if (null == subject || subject.isEmpty()){
            errMsg += " subject";
            log.error("{} {}",beanDescription,errMsg);
            throw new RuntimeException(errMsg);
        }
        if (null == tradeNo || tradeNo.isEmpty()){
            errMsg += " tradeNo";
            log.error("{} {}",beanDescription,errMsg);
            throw new RuntimeException(errMsg);
        }
        if (null == totalAmount){
            errMsg += " totalAmount";
            log.error("{} {}",beanDescription,errMsg);
            throw new RuntimeException(errMsg);
        }

    }
}
