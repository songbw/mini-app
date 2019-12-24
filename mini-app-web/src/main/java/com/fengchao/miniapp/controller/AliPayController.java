package com.fengchao.miniapp.controller;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.client.http.IAggPayClient;
import com.fengchao.miniapp.constant.AliPay;
import com.fengchao.miniapp.constant.ApiType;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.constant.PaymentStatusType;
import com.fengchao.miniapp.dto.WSPayPaymentNotifyBean;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;
import com.fengchao.miniapp.service.impl.AliPaySDKClient;
import com.fengchao.miniapp.service.impl.PaymentServiceImpl;
import com.fengchao.miniapp.service.impl.RefundServiceImpl;
import com.fengchao.miniapp.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Validated
@Api(tags="AliPayAPI", description = "支付宝支付接口", produces = "application/json;charset=UTF-8")
@RestController
@RequestMapping(value = "/aliPay")
@Slf4j
public class AliPayController {

    private AliPaySDKClient aliPaySDKClient;
    private RedisDAO redisDAO;
    private RefundServiceImpl refundService;
    private PaymentServiceImpl paymentService;
    private IAggPayClient aggPayClient;

    @Autowired
    public AliPayController(PaymentServiceImpl paymentService,
                            IAggPayClient aggPayClient,
                            RefundServiceImpl refundService,
                            AliPaySDKClient aliPaySDKClient,
                            RedisDAO redisDAO){

        this.aliPaySDKClient = aliPaySDKClient;
        this.redisDAO = redisDAO;
        this.refundService = refundService;
        this.paymentService = paymentService;
        this.aggPayClient = aggPayClient;

    }
/*
    @ApiOperation(value = "获取支付宝token", notes="获取支付宝token")
    @GetMapping("/token")
    public ResultObject<String>
    getToken(@ApiParam(value="code",required=true)  @Valid @NotBlank(message = MyErrorCode.ALIPAY_SDK_CODE_BLANK) @RequestParam("code")  String code
    )throws Exception{

        String functionDescription = "获取支付宝token";Thread.currentThread().getStackTrace()[1].getMethodName();

        log.info("=== {} 入参 code={}",functionDescription,code);
        ResultObject<String> result = new ResultObject<>(200,"success",null);

        String appId = aliPaySDKClient.getAppId();
        String storedToken = redisDAO.getAliPayToken(appId);
        if (null != storedToken){

            result.setData(storedToken);

            log.info("=== {} 成功 got stored token {}",functionDescription,storedToken);
            return result;
        }

        WeChatTokenResultBean bean;
        try{
            bean = aliPaySDKClient.getToken(code);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            throw e;
        }

        if (null == bean){
            String m = MyErrorCode.ALIPAY_SDK_GET_TOKEN_FAILED;
            log.error("{} {}",functionDescription,m);
            throw new Exception(m);
        }

        String token = bean.getAccess_token();
        result.setData(token);

        redisDAO.storeAliPayToken(token,bean.getExpires_in(),appId);

        log.info("=== {} 成功 token = {} ",functionDescription,token);
        return result;

    }
*/
    @ApiOperation(value = "支付宝下单", notes="支付宝下单")
    @PostMapping("/prepay")
    public String
    postWapPay(HttpServletResponse httpResponse,
               HttpServletRequest request,
        @RequestBody  AliWapPayPostBean data)
        throws Exception {

        String functionDescription = "支付宝外部商户创建订单并支付 ";
        log.info("=== {} 入参 {}", functionDescription, JSON.toJSONString(data));
        data.verifyValue();//fix it

        String form = aliPaySDKClient.postWapPay(data,data.getIAppId());
        if (null == form){
            throw new RuntimeException(MyErrorCode.ALIPAY_SDK_FAILED+"外部商户创建订单并支付 返回空");
        }

        String ip = request.getRemoteAddr();

        Payment payment = new Payment();
        payment.setIp(ip);
        payment.setiAppId(data.getIAppId());
        payment.setApiType(ApiType.ALIPAY_PHONE_WEB.getCode());
        payment.setOrderId(data.getTradeNo());
        payment.setTotalFee(Integer.valueOf(FeeUtils.Yuan2Fen(data.getTotalAmount().toString())));
        payment.setStatus(PaymentStatusType.PREPAY.getCode());
        payment.setCreateTime(new Date());
        payment.setUpdateTime(new Date());
        payment.setComments("完成预下单 ");

        try{
            paymentService.insert(payment);
        }catch (Exception e){
            log.error("数据库插入预下单记录失败");
        }

        httpResponse.setContentType("text/html;charset=" + AliPay.CHARSET);
        //httpResponse.getWriter().write(form);
        //httpResponse.getWriter().flush();

        return form;
    }

    @ApiOperation(value = "支付宝申请退款", notes="支付宝申请退款")
    @PostMapping("/refund")
    public ResultObject<AliPayRefundRespBean>
    postRefund(
            @RequestBody  @Valid AliPayRefundPostBean data)
            throws Exception {

        String functionDescription = "支付宝申请退款 ";
        log.info("=== {} 入参 {}", functionDescription, JSON.toJSONString(data));
        ResultObject<AliPayRefundRespBean> result = new ResultObject<>(200, "success", null);

        String orderId = data.getOrderId();
        String refundFeeStr = data.getRefundFee().toString();
        String refundFeeYuan = FeeUtils.Fen2Yuan(refundFeeStr);
        String totalFeeStr = data.getTotalFee().toString();
        String openId = data.getOpenId();

        PageInfo<Payment> pages;
        try{
            pages = paymentService.queryList(1,1,"id","DESC",
                    null,orderId,null,null);
        }catch (Exception e){
            log.error("{} 查询支付记录失败",functionDescription);
            throw e;
        }

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            String msg = MyErrorCode.ALIPAY_PAYMENT_NULL;
            log.error("{} {}",functionDescription,msg);
            throw new Exception(msg);
        }

        AliPayRefundRespBean bean;
        try {
            bean = aliPaySDKClient.refund(orderId, Float.valueOf(refundFeeYuan),pages.getRows().get(0).getiAppId());
        } catch (Exception e) {
            log.error("{} {}", functionDescription, e.getMessage());
            throw e;
        }

        Refund refund = new Refund();

        refund.setComments("支付宝申请退款成功");
        refund.setApiType(ApiType.ALIPAY_PHONE_WEB.getCode());
        refund.setOrderId(bean.getTradeNo());
        if (null != openId) {
            refund.setOpenId(openId);
        }
        refund.setRefundFee(data.getRefundFee());
        refund.setRefundNo(data.getRefundNo());
        refund.setTransactionId(bean.getAliPayTradeNo());
        refund.setRespRefundFee(Integer.valueOf(FeeUtils.Yuan2Fen(bean.getRefundFee())));
        refund.setTotalFee(Integer.valueOf(totalFeeStr));
        refund.setSettleRefundFee(refund.getRespRefundFee());
        refund.setOpenId(bean.getAliPayLoginId());
        refund.setSuccessTime(DateUtil.Date2String(bean.getRefundDate()));
        refund.setStatus("SUCCESS");
        refund.setCreateTime(new Date());
        refund.setUpdateTime(new Date());

        try {
            refundService.insert(refund);
        } catch (Exception e) {
            log.error("{} 退款记录插入失败 {}", functionDescription, e.getMessage());
        }

        result.setData(bean);

        log.info("=== {}  {}", functionDescription, JSON.toJSONString(refund));
        return result;

    }

    @ApiOperation(value = "支付宝付款回调", notes="支付宝付款回调")
    @PostMapping("/payment/notify")
    public String
    paymentNotify(HttpServletRequest request
                 ) {

        String functionDescription = "支付宝付款回调 ";
        log.info("=== {} 入参 {}", functionDescription, JSON.toJSONString(request));

        String resultOk = "success";
        String resultFailed = "fail";

        Payment payment = aliPaySDKClient.handlePaymentNotify(request);
        if (null == payment){
            log.info("=== {} 失败", functionDescription);
            return resultFailed;
        }
        log.info("=== {} 更新记录成功", functionDescription);

        WSPayPaymentNotifyBean bean = new WSPayPaymentNotifyBean();
        bean.setOrderNo(payment.getOrderId());
        bean.setPayType("fcalipay");
        bean.setTradeNo(payment.getTransactionId());
        if (null != payment.getTimeEnd() && !payment.getTimeEnd().isEmpty()) {
            bean.setTradeDate(payment.getTimeEnd());
        }
        if(null != payment.getTotalFee() && 0 != payment.getTotalFee()) {
            bean.setPayFee(payment.getTotalFee().toString());
        }
        log.info("{} try通知聚合支付付款完成:  {}",functionDescription,JSON.toJSONString(bean));
        try{
            aggPayClient.postAggPayPaymentNotify(bean);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage(),e);
        }
        log.info("{} 通知聚合支付付款 成功",functionDescription);

        return resultOk;
    }


    @ApiOperation(value = "支付宝查询退款结果", notes="支付宝查询退款结果")
    @GetMapping("/refund/status")
    public ResultObject<AliPayRefundQueryResultBean>
    queryRefund(
                @ApiParam(value="refundNo",required=true)
                @RequestParam String refundNo,
                @ApiParam(value="orderId",required=true)
                @RequestParam @NotNull(message = MyErrorCode.WECHAT_API_TRAN_NO_BLANK) String orderId
    ) throws Exception{

        String functionDescription = "支付宝查询退款结果 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: refundNo={} orderId={}",functionDescription, refundNo, orderId);

        PageInfo<Refund> pages = refundService.queryList(1,1,"id","DESC",
                null,refundNo,null,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            String m = MyErrorCode.REFUND_NO_FOUND;
            log.error("{} {}",functionDescription,m);
            throw new Exception(m);
        }

        Refund refund = pages.getRows().get(0);
        AliPayRefundQueryResultBean bean;
        try {
            bean = aliPaySDKClient.queryRefund(refund);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            throw e;
        }
        log.info("=== {} 成功",functionDescription);
        return new ResultObject<>(200,"success",bean);

    }
}
