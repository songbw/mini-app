package com.fengchao.miniapp.controller;


import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.constant.PaymentStatusType;
import com.fengchao.miniapp.constant.WeChat;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;
import com.fengchao.miniapp.model.UserInfo;
import com.fengchao.miniapp.service.impl.PaymentServiceImpl;
import com.fengchao.miniapp.service.impl.RefundServiceImpl;
import com.fengchao.miniapp.service.impl.UserInfoServiceImpl;
import com.fengchao.miniapp.service.impl.WeChatMiniAppClientImpl;
import com.fengchao.miniapp.utils.PageInfo;
import com.fengchao.miniapp.utils.RedisDAO;
import com.fengchao.miniapp.utils.ResultObject;
import com.fengchao.miniapp.utils.XmlUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Validated
@Api(tags="WeChatMiniAppApi", description = "微信小程序接口", produces = "application/json;charset=UTF-8")
@RestController
@RequestMapping(value = "/wechat")
@Slf4j
public class WeChatMiniAppController {

    private WeChatMiniAppClientImpl weChatMiniAppClient;
    private RedisDAO redisDAO;
    private UserInfoServiceImpl userInfoService;
    private PaymentServiceImpl paymentService;
    private RefundServiceImpl refundService;

    @Autowired
    public WeChatMiniAppController(RefundServiceImpl refundService,PaymentServiceImpl paymentService,UserInfoServiceImpl userInfoServic,RedisDAO redisDAO,WeChatMiniAppClientImpl weChatMiniAppClient){

        this.weChatMiniAppClient = weChatMiniAppClient;
        this.redisDAO = redisDAO;
        this.userInfoService = userInfoServic;
        this.paymentService = paymentService;
        this.refundService = refundService;

    }

    @ApiOperation(value = "查询UserInfo", notes="查询UserInfo")
    @GetMapping("/token")
    public ResultObject<String>
    getToken(HttpServletResponse response)throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        ResultObject<String> result = new ResultObject<>(200,"success",null);

        String storedToken = redisDAO.getWeChatToken();
        if (null != storedToken){
            log.info(_func+" ==got stored token "+storedToken);
            result.setData(storedToken);
            response.setStatus(200);
            return result;
        }

        WeChatTokenResultBean bean;
        try{
            bean = weChatMiniAppClient.getAccessToken();
        }catch (Exception e){
            throw new Exception(e);
        }

        if (null == bean.getAccess_token()){
            throw new Exception(MyErrorCode.WECHAT_API_TOKEN_NULL);
        }

        String token = bean.getAccess_token();
        result.setData(token);
        response.setStatus(200);
        redisDAO.storeWeChatToken(token,bean.getExpires_in());
        log.info(_func+"success token= "+token);
        return result;

    }

    @ApiOperation(value = "获取session", notes="获取session")
    @GetMapping("/login")
    public ResultObject<WeChatSessionResultBean>
    getSession(HttpServletResponse response,
               @RequestParam  @Valid @NotBlank(message=MyErrorCode.WECHAT_API_JS_CODE_BLANK) String jsCode)
            throws Exception{

        ResultObject<WeChatSessionResultBean> result = new ResultObject<>(200,"success",null);

        WeChatSessionResultBean bean;
        try{
            bean = weChatMiniAppClient.getSession(jsCode);
        }catch (Exception e){
            throw e;
        }

        UserInfo info = null;
        try{
            info = userInfoService.selectByOpenId(bean.getOpenid());
        }catch (Exception e){
            throw e;
        }

        bean.setIsNewUser((null == info)?"0":"1");
        result.setData(bean);
        response.setStatus(200);

        return result;

    }

    @ApiOperation(value = "预支付交易会话", notes="预支付交易会话")
    @PostMapping("/unifiedOrder")
    public ResultObject<WechatPrepayBean>
    postUnifiedOrder(HttpServletResponse response, HttpServletRequest request,
                     @RequestBody  @Valid WechatOrderPostBean data)
            throws Exception{

        ResultObject<WechatPrepayBean> result = new ResultObject<>(200,"success",null);
        String ip = request.getRemoteAddr();

        WechatPrepayBean bean;
        try{
            bean = weChatMiniAppClient.postPrepayId(data,ip);
        }catch (Exception e){
            throw e;
        }

        Payment payment = new Payment();
        payment.setIp(ip);
        payment.setOpenId(data.getOpenId());
        payment.setOrderId(data.getTradeNo());
        payment.setTotalFee(data.getTotalFee());
        payment.setResult(bean.getResultCode());
        payment.setStatus(PaymentStatusType.PREPAY.getCode());
        payment.setPrepayId(bean.getPrepayId());
        payment.setCreateTime(new Date());
        payment.setUpdateTime(new Date());

        try{
            paymentService.insert(payment);
        }catch (Exception e){
            log.error("预下单记录失败");
        }


        result.setData(bean);
        response.setStatus(200);

        return result;

    }

    @ApiOperation(value = "支付结果通知", notes="支付结果通知")
    @PostMapping("/payment/notify")
    public String
    paymentNotify(HttpServletResponse response,
                     @RequestBody  String xmlStr)
            throws Exception{

        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} 入参: {}",_func,xmlStr);
        Map<String,Object> respMap = new HashMap<>();
        Map<String,Object> failMap = new HashMap<>();
        respMap.put(WeChat.RETURN_CODE_KEY,WeChat.RETURN_CODE_SUCCESS);
        failMap.put(WeChat.RETURN_CODE_KEY,WeChat.RETURN_CODE_FAIL);
        String okXml = XmlUtil.map2xml(respMap);
        String failXml = XmlUtil.map2xml(failMap);

        try{
            boolean isRightSign = weChatMiniAppClient.isRightSign(xmlStr);
            if (!isRightSign){
                log.error(MyErrorCode.WECHAT_NOTIFY_SIGN_WRONG);
                return failXml;
            }
        }catch (Exception e){
            log.error(e.getMessage());
            return failXml;
        }

        Map<String,Object> postMap = XmlUtil.xml2map(xmlStr);

        Object respCodeObj = postMap.get(WeChat.RETURN_CODE_KEY);
        Object respMsgObj = postMap.get(WeChat.RETURN_MESSAGE_KEY);
        if (null == respCodeObj || respCodeObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }
        String respCode = respCodeObj.toString();
        if (!WeChat.RETURN_CODE_SUCCESS.equals(respCode)){
            String msg = (null==respMsgObj)?" ":respMsgObj.toString();
            log.error(MyErrorCode.WECHAT_NOTIFY_ERROR+msg);
            return failXml;
        }

        Object orderIdObj = postMap.get(WeChat.OUT_TRADE_NO_KEY);
        if(null == orderIdObj || orderIdObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_NOTIFY_ERROR+" 商户订单号");
            return failXml;
        }

        Object tranIdObj = postMap.get(WeChat.RESP_TRANSACTION_ID_KEY);
        Object timeEndObj = postMap.get(WeChat.RESP_TIME_END_KEY);
        Object cashFeeObj = postMap.get(WeChat.RESP_CASH_FEE_KEY);
        Object totalFeeObj = postMap.get(WeChat.TOTAL_FEE_KEY);
        Object signObj = postMap.get(WeChat.SIGN_KEY);
        Object resultObj = postMap.get(WeChat.RESULT_CODE_KEY);
        Object openIdObj = postMap.get(WeChat.OPEN_ID_KEY);

        if (null == tranIdObj || null == timeEndObj || null == cashFeeObj
            || null == totalFeeObj || null == signObj || null == resultObj || null == openIdObj
            ||tranIdObj.toString().isEmpty() || timeEndObj.toString().isEmpty()
            ||cashFeeObj.toString().isEmpty() || totalFeeObj.toString().isEmpty()
            || signObj.toString().isEmpty() || resultObj.toString().isEmpty()
            || openIdObj.toString().isEmpty()){

            log.error(MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        String orderId = orderIdObj.toString();
        PageInfo<Payment> pages = null;
        Payment payment = null;
        try{
            pages = paymentService.queryList(1,1,"id","DESC",
                    openIdObj.toString(),orderId,null,null);
        }catch (Exception e){
            throw e;
        }

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error(MyErrorCode.PAYMENT_NO_FOUND);
            return okXml;
        }

        payment = pages.getRows().get(0);
        if (PaymentStatusType.OK.getCode().equals(payment.getStatus())
                || PaymentStatusType.FAILED.getCode().equals(payment.getStatus())){

            log.info("{} 支付结果已经记录,不再重复处理",_func);
            return okXml;
        }

        payment.setResult(resultObj.toString());
        payment.setRespTotalFee(Integer.valueOf(totalFeeObj.toString()));
        payment.setCashFee(Integer.valueOf(cashFeeObj.toString()));
        payment.setTransactionId(tranIdObj.toString());
        payment.setTimeEnd(timeEndObj.toString());
        payment.setUpdateTime(new Date());
        if (WeChat.RETURN_CODE_SUCCESS.equals(resultObj.toString())){
            payment.setStatus(PaymentStatusType.OK.getCode());
        }else{
            payment.setStatus(PaymentStatusType.FAILED.getCode());
            Object errCodeObj = postMap.get(WeChat.RESP_ERR_CODE_KEY);
            Object errMsgObj = postMap.get(WeChat.RESP_ERR_MESSAGE_KEY);
            String msg = " ";
            if (null != errCodeObj && !errCodeObj.toString().isEmpty()){
                msg += errCodeObj.toString();
            }
            if (null != errMsgObj && !errMsgObj.toString().isEmpty()){
                msg += errMsgObj.toString();
            }
            payment.setResult(msg);
        }

        try{
            paymentService.update(payment);
        }catch (Exception e){
            log.error("更新记录失败"+e.getMessage());
        }

        log.info("{} exit 更新记录成功",_func);
        return okXml;
    }

    @ApiOperation(value = "关闭订单", notes="关闭订单")
    @PostMapping("/payment/close")
    public ResultObject<String>
    closePayment(HttpServletResponse response,
                 @RequestBody  @Valid WechatPaymentQueryBean data)
            throws Exception{

        String _func = "关闭订单 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} 入参: {}",_func, JSON.toJSON(data));

        String orderId = data.getOrderId();
        String openId = data.getOpenId();

        PageInfo<Payment>  pages = paymentService.queryList(1,1,"id","DESC",
                openId,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            throw new Exception(MyErrorCode.REFUND_NO_FOUND);
        }

        Payment payment = pages.getRows().get(0);

        boolean isOk = false;
        isOk = weChatMiniAppClient.closePayment(payment);
        if (!isOk){
            return new ResultObject<>(400,"关闭订单失败","");
        }

        payment.setStatus(PaymentStatusType.CLOSED.getCode());
        try {
            paymentService.update(payment);
        }catch (Exception e){
            return new ResultObject<>(200,"关闭订单成功，更新支付记录状态失败","");
        }
        return new ResultObject<>(200,"success","");

    }

    @ApiOperation(value = "查询订单结果", notes="查询订单结果")
    @PostMapping("/payment/query")
    public ResultObject<Payment>
    queryPayment(HttpServletResponse response,
                 @RequestBody  @Valid WechatPaymentQueryBean data)
            throws Exception{

        String _func = "查询退款结果";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} 入参: {}",_func, JSON.toJSON(data));

        String orderId = data.getOrderId();
        String openId = data.getOpenId();

        PageInfo<Payment>  pages = paymentService.queryList(1,1,"id","DESC",
                openId,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            throw new Exception(MyErrorCode.REFUND_NO_FOUND);
        }

        Payment payment = pages.getRows().get(0);

        weChatMiniAppClient.queryPayment(payment);

        return new ResultObject<>(200,"success",payment);

    }

    @ApiOperation(value = "申请退款", notes="申请退款")
    @PostMapping("/refund")
    public ResultObject<WechatRefundRespBean>
    postRefund(HttpServletResponse response,
                     @RequestBody  @Valid WechatRefundPostBean data)
            throws Exception{

        ResultObject<WechatRefundRespBean> result = new ResultObject<>(200,"success",null);

        Refund refund = weChatMiniAppClient.postRefund(data);

        try{
            refundService.insert(refund);
        }catch (Exception e){
            log.error("退款记录插入失败 {}",e.getMessage());
        }

        WechatRefundRespBean bean = new WechatRefundRespBean();
        bean.setRefundNo(refund.getRefundNo());
        bean.setWechatRefundNo(refund.getWechatRefundNo());
        bean.setRefundFee(refund.getRespRefundFee());
        bean.setCode(refund.getStatus());
        bean.setMsg(refund.getComments());

        result.setData(bean);
        response.setStatus(200);

        return result;

    }

    @ApiOperation(value = "退款结果通知", notes="退款结果通知")
    @PostMapping("/refund/notify")
    public String
    refundNotify(HttpServletResponse response,
               @RequestBody  String xmlStr)
            throws Exception{

        String _func = "退款结果通知";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} 入参: {}",_func,xmlStr);

        Map<String,Object> okMap = new HashMap<>();
        Map<String,Object> failMap = new HashMap<>();
        okMap.put(WeChat.RETURN_CODE_KEY,WeChat.RETURN_CODE_SUCCESS);
        failMap.put(WeChat.RETURN_CODE_KEY,WeChat.RETURN_CODE_FAIL);
        String okXml = XmlUtil.map2xml(okMap);
        String failXml = XmlUtil.map2xml(failMap);

        Map<String,Object> postMap = XmlUtil.xml2map(xmlStr);

        Object respCodeObj = postMap.get(WeChat.RETURN_CODE_KEY);
        Object respMsgObj = postMap.get(WeChat.RETURN_MESSAGE_KEY);
        if (null == respCodeObj || respCodeObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }
        String respCode = respCodeObj.toString();
        if (!WeChat.RETURN_CODE_SUCCESS.equals(respCode)){
            String msg = (null==respMsgObj)?" ":respMsgObj.toString();
            log.error(MyErrorCode.WECHAT_NOTIFY_ERROR+msg);
            return failXml;
        }

        Object mchIdObj = postMap.get(WeChat.MERCHANT_ID_KEY);
        if(null == mchIdObj || null == mchIdObj.toString() ||mchIdObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_NOTIFY_ERROR+" 商户号 缺失");
            return failXml;
        }
        if (!WeChat.MINI_APP_PAYMENT_MCH_ID.equals(mchIdObj.toString())){
            log.error(MyErrorCode.WECHAT_NOTIFY_ERROR+" 商户号 错误");
            return failXml;
        }

        Object reqInfoObj = postMap.get(WeChat.NOTIFY_REQ_INFO_KEY);
        if (null == reqInfoObj || null == reqInfoObj.toString() || reqInfoObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_NOTIFY_ERROR+" 加密信息 错误");
            return failXml;
        }

        String reqInfo = weChatMiniAppClient.DecodePkcs7(reqInfoObj.toString());
        Map<String,Object> infoMap = XmlUtil.xml2map(reqInfo);

        Object tranIdObj = infoMap.get(WeChat.RESP_TRANSACTION_ID_KEY);

        Object orderIdObj = infoMap.get(WeChat.OUT_TRADE_NO_KEY);
        Object totalFeeObj = infoMap.get(WeChat.TOTAL_FEE_KEY);
        Object refundFeeObj = infoMap.get(WeChat.REFUND_FEE_KEY);
        Object settleFeeObj = infoMap.get(WeChat.SETTLE_REFUND_FEE_KEY);
        Object refundIdObj = infoMap.get(WeChat.REFUND_ID_KEY);
        Object outRefundNoObj = infoMap.get(WeChat.OUT_REFUND_NO_KEY);
        Object statusObj = infoMap.get(WeChat.REFUND_STATUS_KEY);
        Object recvAccountObj = infoMap.get(WeChat.RECV_ACCOUNT_KEY);
        Object refundAccountObj = infoMap.get(WeChat.REFUND_ACCOUNT_KEY);
        Object requestSourceObj = infoMap.get(WeChat.REFUND_REQUEST_SOURCE_KEY);


        if (null == tranIdObj || null == refundFeeObj || null == settleFeeObj
            || null == totalFeeObj || null == refundIdObj || null == outRefundNoObj
            || null == orderIdObj || null == statusObj || null == recvAccountObj
            || null == refundAccountObj || null == requestSourceObj
            || tranIdObj.toString().isEmpty() || refundFeeObj.toString().isEmpty() || settleFeeObj.toString().isEmpty()
            || statusObj.toString().isEmpty() || totalFeeObj.toString().isEmpty() || refundFeeObj.toString().isEmpty()
            || orderIdObj.toString().isEmpty() || outRefundNoObj.toString().isEmpty() ||refundIdObj.toString().isEmpty()
            || recvAccountObj.toString().isEmpty() || refundAccountObj.toString().isEmpty() || requestSourceObj.toString().isEmpty()
        ){

            log.error(MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        String orderId = orderIdObj.toString();
        String refundNo = outRefundNoObj.toString();
        PageInfo<Refund> pages = null;
        Refund refund = null;
        try{
            pages = refundService.queryList(1,1,"id","DESC",
                    null,refundNo,orderId,null,null);
        }catch (Exception e){
            throw e;
        }

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error(MyErrorCode.REFUND_NO_FOUND);
            return okXml;
        }

        Integer respRefundFee = Integer.valueOf(settleFeeObj.toString());
        refund = pages.getRows().get(0);
        if (respRefundFee.equals(refund.getRespRefundFee())){
            log.info("{} 退款结果已经记录,不再重复处理",_func);
            return okXml;
        }

        Object timeEndObj = infoMap.get(WeChat.SUCCESS_TIME_KEY);
        if (null != timeEndObj){
            refund.setSuccessTime(timeEndObj.toString());
        }
        refund.setTransactionId(tranIdObj.toString());
        refund.setRespRefundFee(respRefundFee);
        refund.setRefundAccount(refundAccountObj.toString());
        refund.setRefundRecvAccount(recvAccountObj.toString());
        refund.setStatus(statusObj.toString());

        refund.setUpdateTime(new Date());
        try{
            refundService.update(refund);
        }catch (Exception e){
            log.error("更新退款记录失败"+e.getMessage());
        }

        log.info("{} exit 更新退款记录成功",_func);
        return okXml;

    }

    @ApiOperation(value = "查询退款结果", notes="查询退款结果")
    @PostMapping("/refund/query")
    public ResultObject<Refund>
    queryRefund(HttpServletResponse response,
                 @RequestBody  @Valid WechatRefundQueryBean data)
            throws Exception{

        String _func = "查询退款结果";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} 入参: {}",_func, JSON.toJSON(data));

        String refundNo = data.getRefundNo();
        String openId = data.getOpenId();

        PageInfo<Refund>  pages = refundService.queryList(1,1,"id","DESC",
                    openId,refundNo,null,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            throw new Exception(MyErrorCode.REFUND_NO_FOUND);
        }

        Refund refund = pages.getRows().get(0);

        weChatMiniAppClient.queryRefund(refund);

        return new ResultObject<>(200,"success",refund);

    }
}
