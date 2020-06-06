package com.fengchao.miniapp.controller;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.client.http.IAggPayClient;
import com.fengchao.miniapp.constant.*;
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
import java.util.HashMap;
import java.util.Map;

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
    public ResultObject<String>
    postWapPay(HttpServletResponse httpResponse,
               HttpServletRequest request,
        @RequestBody  AliWapPayPostBean data) {

        String ip = request.getRemoteAddr();

        Payment payment = new Payment();
        payment.setIp(ip);
        payment.setiAppId(data.getIAppId());
        payment.setApiType(ApiType.ALIPAY_PHONE_WEB.getCode());
        payment.setOrderId(data.getTradeNo());
        payment.setTotalFee(data.getTotalAmount());
        payment.setStatus(PaymentStatusType.PREPAY.getCode());
        payment.setCreateTime(new Date());
        payment.setUpdateTime(new Date());
        payment.setComments("完成预下单 ");

        String functionDescription = "支付宝外部商户创建订单并支付 ";
        log.info("=== {} 入参 {}", functionDescription, JSON.toJSONString(data));
        data.verifyValue();//fix it

        String form = aliPaySDKClient.postWapPay(data,data.getIAppId());
        if (null == form){
            throw new RuntimeException(MyErrorCode.ALIPAY_SDK_FAILED+"外部商户创建订单并支付 返回空");
        }

        try{
            paymentService.insert(payment);
        }catch (Exception e){
            log.error("数据库插入预下单记录失败");
        }

        httpResponse.setContentType("text/html;charset=" + AliPay.CHARSET);
        //httpResponse.getWriter().write(form);
        //httpResponse.getWriter().flush();

        return new ResultObject<>(200,"success",form);
    }

    @ApiOperation(value = "支付宝申请退款", notes="支付宝申请退款")
    @PostMapping("/refund")
    public ResultObject<RefundRespToAggPayBean>
    postRefund(
            @RequestBody  @Valid AliPayRefundPostBean data) {

        String functionDescription = "支付宝申请退款 ";
        log.info("=== {} 入参 {}", functionDescription, JSON.toJSONString(data));
        ResultObject<RefundRespToAggPayBean> result = new ResultObject<>(200, "success", null);

        String orderId = data.getOrderId();
        String refundFeeStr = data.getRefundFee().toString();
        String refundFeeYuan = FeeUtils.Fen2Yuan(refundFeeStr);
        String totalFeeStr = data.getTotalFee().toString();
        String openId = data.getOpenId();
        String refundNo = data.getRefundNo();

        PageInfo<Payment> pages;
        try{
            pages = paymentService.queryList(1,1,"id","DESC",
                    null,orderId,null,null);
        }catch (Exception e){
            log.error("{} 查询 orderId={} 的支付记录失败",functionDescription,orderId);
            throw e;
        }

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            String msg = MyErrorCode.ALIPAY_PAYMENT_NULL;
            log.error("{} {}",functionDescription,msg);
            throw new RuntimeException(msg);
        }

        Refund refund = new Refund();
        refund.setApiType(ApiType.ALIPAY_PHONE_WEB.getCode());
        refund.setiAppId(pages.getRows().get(0).getiAppId());
        refund.setOrderId(orderId);
        refund.setRefundNo(refundNo);
        refund.setRefundFee(data.getRefundFee());

        AliPayRefundRespBean bean =
                aliPaySDKClient.refund(orderId, Float.valueOf(refundFeeYuan),pages.getRows().get(0).getiAppId());

        boolean isRefundFailed = (null == bean.getAliPayTradeNo());
        if (isRefundFailed){
            refund.setRemoteRefundNo(bean.getRemoteRefundNo());
            refund.setComments(bean.getComments());
            refund.setStatus(RefundStatusEnum.FAILED.getCode());
        } else {
            refund.setRemoteRefundNo(bean.getRemoteRefundNo());
            refund.setComments(AliPay.REFUND_OK_COMMENTS);
            refund.setStatus(RefundStatusEnum.SUCCESS.getCode());
            if (null != openId) {
                refund.setOpenId(openId);
            }

            refund.setTransactionId(bean.getAliPayTradeNo());
            refund.setRespRefundFee(Integer.valueOf(FeeUtils.Yuan2Fen(bean.getRefundFee())));
            refund.setTotalFee(Integer.valueOf(totalFeeStr));
            refund.setSettleRefundFee(refund.getRespRefundFee());
            if (null != bean.getBuyerId()) {
                refund.setOpenId(bean.getBuyerId());
            } else if (null != bean.getAliPayLoginId()) {
                refund.setOpenId(bean.getAliPayLoginId());
            }
            refund.setSuccessTime(DateUtil.Date2String(bean.getRefundDate()));

        }
        refund.setCreateTime(new Date());
        refund.setUpdateTime(new Date());

        try {
            refundService.insert(refund);
        } catch (Exception e) {
            log.error("{} 退款记录插入失败 {}", functionDescription, e.getMessage());
        }

        RefundRespToAggPayBean respBean = new RefundRespToAggPayBean();
        respBean.setRefundNo(refund.getRefundNo());
        respBean.setRemoteRefundNo(refund.getRemoteRefundNo());
        respBean.setRefundFee(refund.getRespRefundFee());
        respBean.setCode(refund.getStatus());
        respBean.setMsg(refund.getComments());
        result.setData(respBean);

        log.info("=== {} 完成 {}", functionDescription, JSON.toJSONString(refund));
        return result;

    }

    @ApiOperation(value = "支付宝付款回调", notes="支付宝付款回调")
    @PostMapping("/payment/notify")
    public String
    paymentNotify(/*@RequestParam Map<String,String> params*/
            HttpServletRequest request
                  ) {

        String functionDescription = "支付宝付款回调 ";


        Map<String,String> params = aliPaySDKClient.request2Map(request);
        log.info("=== {} 入参 {}", functionDescription, JSON.toJSONString(params));

        String resultOk = "success";
        String resultFailed = "fail";

        Payment payment = aliPaySDKClient.handlePaymentNotify(params);
        if (null == payment){
            log.info("=== {} 失败", functionDescription);
            return resultFailed;
        }
        if (MyErrorCode.NOTIFY_HANDLED_IP_DONE.equals(payment.getIp())){
            //不需要重复处理，或该回调是退款回调,不回调聚合支付
            return resultOk;
        }
        log.info("=== {} 更新记录成功 {}", functionDescription,JSON.toJSONString(payment));

        if(!PaymentStatusType.OK.getCode().equals(payment.getStatus())){
            ///不是成功支付的回调不通知聚合支付
            log.info("{} 不是成功支付的回调 不通知聚合支付",functionDescription);
            return resultOk;
        }

        WSPayPaymentNotifyBean bean = new WSPayPaymentNotifyBean();
        bean.setOrderNo(payment.getOrderId());
        bean.setPayType(AliPay.AGGPAY_BANK_TYPE_FOR_ALIPAY);
        bean.setTradeNo(payment.getTransactionId());
        if (null != payment.getTimeEnd() && !payment.getTimeEnd().isEmpty()) {
            bean.setTradeDate(payment.getTimeEnd());
        }else{
            bean.setTradeDate(DateUtil.Date2String(new Date()));
        }
        if(null != payment.getTotalFee() && 0 != payment.getTotalFee()) {
            bean.setPayFee(payment.getTotalFee().toString());
        }
        log.info("{} 开始通知聚合支付付款完成:  {}",functionDescription,JSON.toJSONString(bean));
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
    public ResultObject<PageInfo<Refund>>
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

        pages.getRows().forEach(refund->{
            if (RefundStatusEnum.PENDING.getCode().equals(refund.getStatus())) {
                aliPaySDKClient.queryRefund(refund);
            }
        });

        log.info("=== {} 成功",functionDescription);
        return new ResultObject<>(200,"success",pages);

    }

    @ApiOperation(value = "支付宝查询订单状态", notes="支付宝查询订单状态")
    @GetMapping("/payment/status")
    public ResultObject<PageInfo<Payment>>
    queryPayment(
            @ApiParam(value="orderId",required=true)
            @RequestParam @NotNull(message = MyErrorCode.WECHAT_API_TRAN_NO_BLANK) String orderId
    ) throws Exception{

        String functionDescription = "支付宝查询订单状态 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: orderId={}",functionDescription, orderId);

        PageInfo<Payment> pages = paymentService.queryList(1,1,"id","DESC",
                null,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            String m = MyErrorCode.PAYMENT_NO_FOUND;
            log.error("{} {}",functionDescription,m);
            throw new Exception(m);
        }

        pages.getRows().forEach(record->{
            if (PaymentStatusType.PREPAY.getCode().equals(record.getStatus()) ||
                    PaymentStatusType.USERPAYING.getCode().equals(record.getStatus())) {
                aliPaySDKClient.queryPayment(record);
            }
        });

        log.info("=== {} 成功",functionDescription);
        return new ResultObject<>(200,"success",pages);

    }
}
