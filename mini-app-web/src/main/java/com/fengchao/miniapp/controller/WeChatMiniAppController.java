package com.fengchao.miniapp.controller;


import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.client.http.IAggPayClient;
import com.fengchao.miniapp.constant.*;
import com.fengchao.miniapp.dto.WSPayPaymentNotifyBean;
import com.fengchao.miniapp.dto.WSPayRefundNotifyBean;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;
import com.fengchao.miniapp.model.UserInfo;
import com.fengchao.miniapp.service.impl.PaymentServiceImpl;
import com.fengchao.miniapp.service.impl.RefundServiceImpl;
import com.fengchao.miniapp.service.impl.UserInfoServiceImpl;
import com.fengchao.miniapp.service.impl.WeChatMiniAppClientImpl;
import com.fengchao.miniapp.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    private IAggPayClient aggPayClient;

    @Autowired
    public WeChatMiniAppController(IAggPayClient aggPayClient,
                                   RefundServiceImpl refundService,
                                   PaymentServiceImpl paymentService,
                                   UserInfoServiceImpl userInfoServic,
                                   RedisDAO redisDAO,
                                   WeChatMiniAppClientImpl weChatMiniAppClient){

        this.weChatMiniAppClient = weChatMiniAppClient;
        this.redisDAO = redisDAO;
        this.userInfoService = userInfoServic;
        this.paymentService = paymentService;
        this.refundService = refundService;
        this.aggPayClient = aggPayClient;

    }

    private void checkApiType(String iAppId,String apiType){
        if (null == iAppId || iAppId.isEmpty()){
            log.error("iAppId缺失!");
            throw new RuntimeException(MyErrorCode.I_APP_ID_BLANK);
        }
        if (!ApiType.isValidCode(apiType)){
            log.error("不支持的API类型: {}",apiType);
            throw new RuntimeException(MyErrorCode.API_TYPE_INVALID+apiType);
        }
    }

    @ApiOperation(value = "获取token", notes="获取token")
    @GetMapping("/token/{apiType}")
    public ResultObject<String>
    getToken(@ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
             @RequestParam  String iAppId
            )throws Exception{

        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        checkApiType(iAppId,apiType);

        log.info("=== {}-{} enter: iAppId={} apiType={}",apiType,functionDescription,iAppId,apiType);
        ResultObject<String> result = new ResultObject<>(200,"success",null);
        String appId = weChatMiniAppClient.getAppId(apiType,iAppId);
        String storedToken = redisDAO.getWeChatToken(appId);
        if (null != storedToken){
            result.setData(storedToken);
            log.info("=== {} 成功 got stored token {}",functionDescription,storedToken);
            return result;
        }

        WeChatTokenResultBean bean;
        try{
            bean = weChatMiniAppClient.getAccessToken(apiType,iAppId);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            throw new RuntimeException(e);
        }

        if (null == bean.getAccess_token()){
            String m = MyErrorCode.WECHAT_API_TOKEN_NULL;
            log.error("{} {}",functionDescription,m);
            throw new Exception(m);
        }

        String token = bean.getAccess_token();
        result.setData(token);

        redisDAO.storeWeChatToken(token,bean.getExpires_in(),appId);

        log.info("=== {} 成功 token = {} ",functionDescription,token);
        return result;

    }

    @ApiOperation(value = "获取session", notes="获取session")
    @GetMapping("/login/{apiType}")
    public ResultObject<WeChatSessionResultBean>
    getSession(@ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
                @RequestParam  @Valid @NotBlank(message=MyErrorCode.WECHAT_API_JS_CODE_BLANK) String jsCode,
               @RequestParam  @Valid @NotBlank(message=MyErrorCode.I_APP_ID_BLANK) String iAppId
            ) throws Exception{

        String functionDescription = "login";
        if (null == apiType ||!ApiType.isValidCode(apiType)){
            log.error("{} 不支持的API类型 {}",functionDescription,apiType);
            return new ResultObject<>(400,"不支持的API类型:"+apiType,null);
        }
        log.info("==={} 参数 jsCode={}",functionDescription,jsCode);
        ResultObject<WeChatSessionResultBean> result = new ResultObject<>(200,"success",null);

        WeChatSessionResultBean bean;
        try{
            bean = weChatMiniAppClient.getSession(jsCode,apiType,iAppId);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            throw e;
        }

        UserInfo info ;
        try{
            info = userInfoService.selectByOpenId(bean.getOpenid());
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            throw e;
        }

        bean.setIsNewUser((null == info)?"1":"0");//1:是新用户 0:是老用户
        result.setData(bean);
        log.info("==={} 成功 {}",functionDescription,JSON.toJSONString(bean));
        return result;

    }

    @ApiOperation(value = "预支付交易会话", notes="预支付交易会话")
    @PostMapping("/unifiedOrder/{apiType}")
    public ResultObject<WechatPrepayBean>
    postUnifiedOrder(HttpServletRequest request,
                     @ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
                     @RequestBody  @Valid WechatOrderPostBean data)
            throws Exception{

        if (!ApiType.isValidCode(apiType)){
            log.error("不支持的API类型 {}",apiType);
            return new ResultObject<>(400,"不支持的API类型:"+apiType,null);
        }
        String functionDescription = apiType+"统一下单 ";
        log.info("=== {} 入参 {}",functionDescription,JSON.toJSONString(data));
        if (null == data.getIAppId() || data.getIAppId().isEmpty()){
            String msg = MyErrorCode.I_APP_ID_BLANK;
            log.error("{} {}",functionDescription,msg);
            return new ResultObject<>(400,msg,null);
        }
        ResultObject<WechatPrepayBean> result = new ResultObject<>(200,"success",null);
        String ip = request.getRemoteAddr();
        String iAppId = data.getIAppId();
        WechatPrepayBean bean;
        try{
            bean = weChatMiniAppClient.postPrepayId(data,ip,apiType,iAppId);
        }catch (Exception e){
            log.error("{} 失败 {}",functionDescription,e.getMessage());
            throw e;
        }

        Payment payment = new Payment();
        payment.setiAppId(iAppId);
        payment.setIp(ip);
        payment.setApiType(apiType);
        payment.setOpenId(data.getOpenId());
        payment.setOrderId(data.getTradeNo());
        payment.setTotalFee(data.getTotalFee());
        payment.setStatus(PaymentStatusType.PREPAY.getCode());
        payment.setPrepayId(bean.getPrepayId());
        payment.setCreateTime(new Date());
        payment.setUpdateTime(new Date());
        payment.setComments("完成统一下单 "+bean.getResult());

        try{
            paymentService.insert(payment);
        }catch (Exception e){
            log.error("数据库插入预下单记录失败");
        }

        result.setData(bean);

        log.info("=== {} 成功 {}",functionDescription,JSON.toJSONString(bean));
        return result;

    }

    @ApiOperation(value = "支付结果通知", notes="支付结果通知")
    @PostMapping("/payment/notify")
    public String
    paymentNotify(@RequestBody  String xmlStr)
            throws Exception{

        String functionDescription = "支付结果通知 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: {}",functionDescription,xmlStr);
        Map<String,Object> respMap = new HashMap<>();
        Map<String,Object> failMap = new HashMap<>();
        respMap.put(WeChat.RETURN_CODE_KEY,WeChat.RETURN_CODE_SUCCESS);
        failMap.put(WeChat.RETURN_CODE_KEY,WeChat.RETURN_CODE_FAIL);
        String okXml = XmlUtil.map2xml(respMap);
        String failXml = XmlUtil.map2xml(failMap);

        Map<String,Object> postMap ;
        try{
            postMap = weChatMiniAppClient.verifySign(xmlStr);
            if (null == postMap){
                log.error("{} {}",functionDescription,MyErrorCode.WECHAT_NOTIFY_SIGN_WRONG);
                return failXml;
            }
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            return failXml;
        }

        Object respCodeObj = postMap.get(WeChat.RETURN_CODE_KEY);
        Object respMsgObj = postMap.get(WeChat.RETURN_MESSAGE_KEY);
        if (null == respCodeObj || respCodeObj.toString().isEmpty()){
            log.error("{} {} {}",functionDescription, MyErrorCode.WECHAT_NOTIFY_ERROR, WeChat.RETURN_CODE_KEY);
            return failXml;
        }
        String respCode = respCodeObj.toString();
        if (!WeChat.RETURN_CODE_SUCCESS.equals(respCode)){
            String msg = (null==respMsgObj)?" ":respMsgObj.toString();
            log.error("{} {} {}",functionDescription,MyErrorCode.WECHAT_NOTIFY_ERROR,msg);
            return failXml;
        }

        Object orderIdObj = postMap.get(WeChat.OUT_TRADE_NO_KEY);
        if(null == orderIdObj || orderIdObj.toString().isEmpty()){
            log.error("{} {}  商户订单号",functionDescription,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        Object tranIdObj = postMap.get(WeChat.RESP_TRANSACTION_ID_KEY);
        Object timeEndObj = postMap.get(WeChat.RESP_TIME_END_KEY);
        Object cashFeeObj = postMap.get(WeChat.RESP_CASH_FEE_KEY);
        Object totalFeeObj = postMap.get(WeChat.TOTAL_FEE_KEY);
        Object resultObj = postMap.get(WeChat.RESULT_CODE_KEY);
        Object openIdObj = postMap.get(WeChat.OPEN_ID_KEY);

        if (null == tranIdObj ||  null == cashFeeObj
            || null == totalFeeObj || null == resultObj || null == openIdObj
            ||tranIdObj.toString().isEmpty()
            ||cashFeeObj.toString().isEmpty() || totalFeeObj.toString().isEmpty()
            || resultObj.toString().isEmpty()
            || openIdObj.toString().isEmpty()){

            log.error("{} {} 参数缺失",functionDescription,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        String orderId = orderIdObj.toString();
        PageInfo<Payment> pages ;
        Payment payment ;
        try{
            pages = paymentService.queryList(1,1,"id","DESC",
                    openIdObj.toString(),orderId,null,null);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            throw e;
        }

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",functionDescription,MyErrorCode.PAYMENT_NO_FOUND);
            return okXml;
        }

        payment = pages.getRows().get(0);
        if (PaymentStatusType.OK.getCode().equals(payment.getStatus())
                || PaymentStatusType.FAILED.getCode().equals(payment.getStatus())){

            log.info("{} 支付结果已经记录,不再重复处理",functionDescription);
            return okXml;
        }

        Integer respTotalFee = Integer.valueOf(totalFeeObj.toString());
        if (!respTotalFee.equals(payment.getTotalFee())){
            log.error("{} 订单金额是与商户侧的订单金额不一致",functionDescription);
            return failXml;
        }

        payment.setResult(resultObj.toString());
        payment.setRespTotalFee(respTotalFee);
        payment.setCashFee(Integer.valueOf(cashFeeObj.toString()));
        payment.setTransactionId(tranIdObj.toString());
        //////回调中没有发现支付完成时间，以通知时间为准,聚合支付需要这个时间
        if(null == timeEndObj) {
            payment.setTimeEnd(DateUtil.Date2String(new Date()));
        }else {
            payment.setTimeEnd(timeEndObj.toString());
        }
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
        payment.setComments("收到支付结果通知");
        try{
            paymentService.update(payment);
        }catch (Exception e){
            log.error("{} 更新记录失败 {}",functionDescription,e.getMessage());
        }

        log.info("=== {} 更新记录成功",functionDescription);

        if(!PaymentStatusType.OK.getCode().equals(payment.getStatus())){
            ///不是成功支付的回调不通知聚合支付
            log.info("{} 不是成功支付的回调 不通知聚合支付",functionDescription);
            return okXml;
        }
        
        WSPayPaymentNotifyBean bean = new WSPayPaymentNotifyBean();
        bean.setOrderNo(payment.getOrderId());
        bean.setPayType(weChatMiniAppClient.getPayType(payment.getApiType()));
        bean.setTradeNo(tranIdObj.toString());
        bean.setTradeDate(payment.getTimeEnd());
        String totalFee = totalFeeObj.toString();
        /*
        int feeSize = totalFee.length();
        String totalFeeYuan;
        if (2 < feeSize){
            totalFeeYuan = totalFee.substring(0,feeSize-2)+"."+totalFee.substring(feeSize-2);
        } else {
            if (2 == feeSize) {
                totalFeeYuan = "0." + totalFee;
            }else if(1 == feeSize){
                totalFeeYuan = "0.0"+totalFee;
            }else{
                log.error("{} 通知信息中付款金额为空",functionDescription);
                totalFeeYuan = "0.00";
            }
        }
        bean.setPayFee(totalFeeYuan);
        */
        bean.setPayFee(totalFee);
        log.info("{} try通知聚合支付付款完成:  {}",functionDescription,JSON.toJSONString(bean));
        try{
            aggPayClient.postAggPayPaymentNotify(bean);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage(),e);
        }
        log.info("{} 通知聚合支付付款 成功",functionDescription);

        return okXml;
    }

    @ApiOperation(value = "支付成功更新订单", notes="支付成功更新订单")
    @PostMapping("/payment/status/{apiType}")
    public ResultObject<String>
    updatePayment(@ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
            @RequestBody  @Valid WechatPaymentQueryBean data)
            throws Exception{

        String functionDescription = "支付成功更新订单状态 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: {}",functionDescription, JSON.toJSON(data));

        String orderId = data.getOrderId();
        String openId = data.getOpenId();

        PageInfo<Payment>  pages = paymentService.queryList(1,1,"id","DESC",
                openId,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",functionDescription, MyErrorCode.PAYMENT_NO_FOUND);
            throw new Exception(MyErrorCode.PAYMENT_NO_FOUND);
        }

        Payment payment = pages.getRows().get(0);
        if (PaymentStatusType.PREPAY.getCode().equals(payment.getStatus())){
            log.info("{} 需要调用微信接口查询订单状态");
            try {
                weChatMiniAppClient.queryPayment(payment,apiType);
            }catch (Exception e){
                log.error("{} {}",functionDescription,e.getMessage());
                throw e;
            }
            //Todo 更新订单状态
        }

        log.info("=== {} 成功",functionDescription);
        return new ResultObject<>(200,"success","");


    }

    @ApiOperation(value = "关闭订单", notes="关闭订单")
    @PostMapping("/payment/close/{apiType}")
    public ResultObject<String>
    closePayment(@ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
                @RequestBody  @Valid WechatPaymentQueryBean data)
            throws Exception{

        if (!ApiType.isValidCode(apiType)){
            log.error("不支持的API类型 {}",apiType);
            return new ResultObject<>(400,"不支持的API类型:"+apiType,null);
        }
        String functionDescription = apiType + "关闭订单 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: {}",functionDescription, JSON.toJSON(data));

        String orderId = data.getOrderId();
        String openId = data.getOpenId();

        PageInfo<Payment>  pages = paymentService.queryList(1,1,"id","DESC",
                openId,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",functionDescription, MyErrorCode.PAYMENT_NO_FOUND);
            throw new Exception(MyErrorCode.PAYMENT_NO_FOUND);
        }

        Payment payment = pages.getRows().get(0);

        boolean isOk ;
        isOk = weChatMiniAppClient.closePayment(payment,apiType);
        if (!isOk){
            log.error("{} 失败",functionDescription);
            return new ResultObject<>(400,"关闭订单失败","");
        }

        payment.setStatus(PaymentStatusType.CLOSED.getCode());
        payment.setComments("订单已经关闭");
        try {
            paymentService.update(payment);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            return new ResultObject<>(200,"关闭订单成功，更新支付记录状态失败","");
        }
        log.info("=== {} 成功",functionDescription);
        return new ResultObject<>(200,"success","");

    }

    @ApiOperation(value = "查询订单状态", notes="查询订单状态")
    @GetMapping("/payment/{apiType}")
    public ResultObject<Payment>
    queryPayment(@ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
                 @ApiParam(value="openId",required=true)
                 @RequestParam @NotNull(message = MyErrorCode.OPEN_ID_BLANK) String openId,
                 @ApiParam(value="orderId",required=true)
                 @RequestParam @NotNull(message = MyErrorCode.WECHAT_API_TRAN_NO_BLANK) String orderId
                )throws Exception{

        if (!ApiType.isValidCode(apiType)){
            log.error("不支持的API类型 {}",apiType);
            return new ResultObject<>(400,"不支持的API类型:"+apiType,null);
        }
        String functionDescription = apiType+"查询订单状态 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: orderId={}",functionDescription, orderId);

        PageInfo<Payment>  pages = paymentService.queryList(1,1,"id","DESC",
                openId,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",functionDescription,MyErrorCode.REFUND_NO_FOUND);
            throw new Exception(MyErrorCode.REFUND_NO_FOUND);
        }

        Payment payment = pages.getRows().get(0);

        if (PaymentStatusType.PREPAY.getCode().equals(payment.getStatus())){
            log.info("{} 需要调用微信接口查询订单状态");
        }else{
            log.info("=== {} 成功找到记录 {}",functionDescription, JSON.toJSONString(payment));
            return new ResultObject<>(200,"success",payment);
        }

        try {
            weChatMiniAppClient.queryPayment(payment,apiType);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            throw e;
        }
        log.info("=== {} 成功",functionDescription);
        return new ResultObject<>(200,"success",payment);

    }

    @ApiOperation(value = "申请退款", notes="申请退款")
    @PostMapping("/refund/{apiType}")
    public ResultObject<RefundRespToAggPayBean>
    postRefund(
               @ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
               @RequestBody  @Valid WechatRefundPostBean data)
            throws Exception{

        if (!ApiType.isValidCode(apiType)){
            log.error("不支持的API类型 {}",apiType);
            return new ResultObject<>(400,"不支持的API类型:"+apiType,null);
        }

        String functionDescription = apiType+"申请退款 ";
        log.info("=== {} 入参 {}",functionDescription,JSON.toJSONString(data));
        ResultObject<RefundRespToAggPayBean> result = new ResultObject<>(200,"success",null);

        String orderId = data.getOrderId();
        String openId = data.getOpenId();
        PageInfo<Payment>  pages = paymentService.queryList(1,1,"id","DESC",
                openId,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            String m = MyErrorCode.PAYMENT_NO_FOUND;
            log.error("{} {}",functionDescription,m);
            throw new Exception(m);
        }

        Payment payment = pages.getRows().get(0);
        Integer paymentStatus = payment.getStatus();
        if (!PaymentStatusType.OK.getCode().equals(paymentStatus)){
            String msg = MyErrorCode.PAYMENT_FAILED;
            log.error("{} {}",functionDescription,msg);
            throw new Exception(msg);
        }

        Refund refund = new Refund();
        BeanUtils.copyProperties(data,refund);
        refund.setApiType(apiType);
        refund.setRefundNo(data.getRefundNo());
        refund.setiAppId(payment.getiAppId());
        refund.setCreateTime(new Date());
        refund.setUpdateTime(new Date());
        refund.setStatus(RefundStatusEnum.PENDING.getCode());
        try {
            refundService.insert(refund);
        } catch (Exception e) {
            log.error("{} 退款记录插入失败 {}", functionDescription, e.getMessage());
            throw new RuntimeException(MyErrorCode.MYSQL_OPERATE_EXCEPTION);
        }

        try {
            refund = weChatMiniAppClient.postRefund(refund,data,apiType,payment.getiAppId());
        }catch (Exception e){
            log.error("{} {}", functionDescription,e.getMessage());
            refund.setStatus(RefundStatusEnum.FAILED.getCode());
            refund.setUpdateTime(new Date());
            try{
                refundService.update(refund);
            }catch (Exception ex){
                log.error(ex.getMessage(),ex);
            }
            throw e;
        }

        String refundStatus = refund.getStatus();
        Long recordId = refund.getId();
        if (WeChat.RETURN_CODE_SUCCESS.equals(refundStatus)) {
            refund.setComments("申请退款成功,等待结果中");
            try {
                Refund record = refundService.getRecordById(recordId);
                if (null != record) {
                    if (RefundStatusEnum.SUCCESS.getCode().equals(record.getStatus()) &&
                            RefundStatusEnum.FAILED.getCode().equals(record.getStatus())) {
                        refund.setStatus(record.getStatus());
                    }
                    refundService.update(refund);
                }
            } catch (Exception e) {
                log.error("{} 退款记录插入失败 {}", functionDescription, e.getMessage());
            }
        }
        RefundRespToAggPayBean bean = new RefundRespToAggPayBean();
        bean.setRefundNo(refund.getRefundNo());
        bean.setRemoteRefundNo(refund.getRemoteRefundNo());
        bean.setRefundFee(refund.getRespRefundFee());
        bean.setCode(refund.getStatus());
        bean.setMsg(refund.getComments());

        result.setData(bean);

        log.info("=== {}  {}",functionDescription, refund.getStatus());
        return result;

    }

    @ApiOperation(value = "退款结果通知", notes="退款结果通知")
    @PostMapping("/refund/notify")
    public String
    refundNotify(@RequestBody  String xmlStr)
            throws Exception{

        String functionDescription = "退款结果通知 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: {}",functionDescription,xmlStr);

        Map<String,Object> okMap = new HashMap<>();
        Map<String,Object> failMap = new HashMap<>();
        okMap.put(WeChat.RETURN_CODE_KEY,WeChat.RETURN_CODE_SUCCESS);
        failMap.put(WeChat.RETURN_CODE_KEY,WeChat.RETURN_CODE_FAIL);
        String okXml = XmlUtil.map2xml(okMap);
        String failXml = XmlUtil.map2xml(failMap);

        Map<String,Object> postMap = XmlUtil.xml2map(xmlStr);

        Object respCodeObj = postMap.get(WeChat.RETURN_CODE_KEY);
        Object respMsgObj = postMap.get(WeChat.RETURN_MESSAGE_KEY);
        if (null == respCodeObj){
            log.error("{} {}",functionDescription,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }
        String respCode = respCodeObj.toString();
        if (!WeChat.RETURN_CODE_SUCCESS.equals(respCode)){
            String msg = (null==respMsgObj)?" ":respMsgObj.toString();
            log.error("{} {} {}",functionDescription, MyErrorCode.WECHAT_NOTIFY_ERROR,msg);
            return failXml;
        }

        Object mchIdObj = postMap.get(WeChat.MERCHANT_ID_KEY);
        if(null == mchIdObj || null == mchIdObj.toString() ||mchIdObj.toString().isEmpty()){
            log.error("{} {} 商户号 缺失",functionDescription,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }
        if (!WeChat.MINI_APP_PAYMENT_MCH_ID.equals(mchIdObj.toString())){
            log.error("{} {} 商户号 错误",functionDescription,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        Object reqInfoObj = postMap.get(WeChat.NOTIFY_REQ_INFO_KEY);
        if (null == reqInfoObj || null == reqInfoObj.toString() || reqInfoObj.toString().isEmpty()){
            log.error("{} {} 加密信息 错误",functionDescription,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        String reqInfo;
        try {
            reqInfo = weChatMiniAppClient.DecodePkcs7(reqInfoObj.toString());
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            return failXml;
        }
        if (null == reqInfo){
            log.error("{} 解密失败",functionDescription);
            return failXml;
        }
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

            log.error("{} {} 参数缺失 ",functionDescription,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        String orderId = orderIdObj.toString();
        String forWxRefundNo = outRefundNoObj.toString();
        PageInfo<Refund> pages ;
        Refund refund ;
        try{
            pages = refundService.queryList(1,1,"id","DESC",
                    null,null,forWxRefundNo,orderId,null,null);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            throw e;
        }

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",functionDescription,MyErrorCode.REFUND_NO_FOUND);
            return okXml;
        }

        Integer settleRefundFee = Integer.valueOf(settleFeeObj.toString());
        Integer respRefundFee = Integer.valueOf(refundFeeObj.toString());
        refund = pages.getRows().get(0);
        if (!refund.getRefundRecvAccount().isEmpty() && !refund.getRefundAccount().isEmpty()){
            log.info("{} 退款结果通知已经记录,不再重复处理",functionDescription);
            return okXml;
        }

        if (!refund.getTransactionId().equals(tranIdObj.toString())){
            log.error("{} {}: {} 与退款记录中 {} 不匹配",
                    functionDescription,WeChat.RESP_TRANSACTION_ID_KEY,tranIdObj.toString(),refund.getTransactionId());
            return failXml;
        }

        if (!respRefundFee.equals(refund.getRefundFee())){
            log.warn("{} {}: {} 与退款记录中 {} 不相等",
                    functionDescription,WeChat.REFUND_FEE_KEY,respRefundFee,refund.getRefundFee());

        }

        Object timeEndObj = infoMap.get(WeChat.SUCCESS_TIME_KEY);
        if (null != timeEndObj){
            refund.setSuccessTime(timeEndObj.toString());
        }

        refund.setSettleRefundFee(settleRefundFee);
        refund.setRefundAccount(refundAccountObj.toString());
        refund.setRefundRecvAccount(recvAccountObj.toString());
        refund.setStatus(statusObj.toString());

        refund.setUpdateTime(new Date());
        refund.setComments("收到退款结果通知");
        try{
            refundService.update(refund);
        }catch (Exception e){
            log.error("{} 更新退款记录失败 {}",functionDescription,e.getMessage());
        }

        log.info("=== {} 更新退款记录成功",functionDescription);

        WSPayRefundNotifyBean bean = new WSPayRefundNotifyBean();
        bean.setRefundNo(refund.getRefundNo());
        bean.setOrderNo(refund.getOrderId());
        bean.setPayType(weChatMiniAppClient.getPayType(refund.getApiType()));
        bean.setTradeNo(refund.getRemoteRefundNo());
        if (null != timeEndObj) {
            bean.setTradeDate(timeEndObj.toString());
        }
        /*
        String totalFee = settleFeeObj.toString();
        int feeSize = totalFee.length();
        String totalFeeYuan;
        if (2 < feeSize){
            totalFeeYuan = totalFee.substring(0,feeSize-2)+"."+totalFee.substring(feeSize-2);
        } else {
            if (2 == feeSize) {
                totalFeeYuan = "0." + totalFee;
            }else if(1 == feeSize){
                totalFeeYuan = "0.0"+totalFee;
            }else{
                log.error("{} 通知信息中退款金额为空",functionDescription);
                totalFeeYuan = "0.00";
            }
        }
        bean.setRefundFee(totalFeeYuan);
        */
        bean.setRefundFee(settleFeeObj.toString());
        log.info("{} try通知聚合支付退款完成:  {}",functionDescription,JSON.toJSONString(bean));
        try{
            aggPayClient.postAggPayRefundNotify(bean);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage(),e);
        }
        log.info("{} 通知聚合支付退款 成功",functionDescription);
        return okXml;

    }

    @ApiOperation(value = "查询退款结果", notes="查询退款结果")
    @GetMapping("/refund/{apiType}")
    public ResultObject<WechatRefundListBean>
    queryRefund(@ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
                @ApiParam(value="openId",required=true)
                @RequestParam @NotNull(message = MyErrorCode.OPEN_ID_BLANK) String openId,
                @ApiParam(value="orderId",required=true)
                @RequestParam @NotNull(message = MyErrorCode.WECHAT_API_TRAN_NO_BLANK) String orderId
            ) throws Exception{

        if (!ApiType.isValidCode(apiType)){
            log.error("不支持的API类型 {}",apiType);
            return new ResultObject<>(400,"不支持的API类型:"+apiType,null);
        }
        String functionDescription = apiType+"查询退款结果 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: openId={} orderId={}",functionDescription, openId, orderId);

        PageInfo<Refund>  pages = refundService.queryList(1,1,"id","DESC",
                    openId,null,null,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            String m = MyErrorCode.REFUND_NO_FOUND;
            log.error("{} {}",functionDescription,m);
            throw new Exception(m);
        }

        Refund refund = pages.getRows().get(0);
        WechatRefundListBean refundListBean = new WechatRefundListBean();
        refundListBean.setOrderId(refund.getOrderId());
        refundListBean.setTransactionId(refund.getTransactionId());
        refundListBean.setTotalFee(refund.getTotalFee());
        refundListBean.setCashFee(refund.getCashFee());
        try {
            weChatMiniAppClient.queryRefund(refundListBean,apiType,refund.getiAppId());
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            throw e;
        }
        log.info("=== {} 成功",functionDescription);
        return new ResultObject<>(200,"success",refundListBean);

    }

    @ApiOperation(value = "查询退款状态", notes="查询退款状态")
    @GetMapping("/refund/{apiType}/{refundNo}")
    public ResultObject<Refund>
    getRefundStatus(@ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
            @ApiParam(value="openId",required=true)
            @RequestParam @NotNull(message = MyErrorCode.OPEN_ID_BLANK) String openId,
            @ApiParam(value="refundNo",required=true)
            @PathVariable("refundNo") @NotBlank(message = MyErrorCode.REFUND_NO_BLANK) String refundNo
    ) throws Exception{

        if (!ApiType.isValidCode(apiType)){
            log.error("不支持的API类型 {}",apiType);
            return new ResultObject<>(400,"不支持的API类型:"+apiType,null);
        }
        String functionDescription = "查询退款状态 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: openId={} refundNo={}",functionDescription, openId, refundNo);

        PageInfo<Refund>  pages = refundService.queryList(1,1,"id","DESC",
                openId,refundNo,null,null,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            String m = MyErrorCode.REFUND_NO_FOUND;
            log.error("{} {}",functionDescription,m);
            throw new Exception(m);
        }

        Refund refund = pages.getRows().get(0);
        if (!refund.getRefundRecvAccount().isEmpty() && !refund.getRefundAccount().isEmpty()){
            log.info("{} 退款结果通知已经记录,不再重复处理",functionDescription);
            return new ResultObject<>(200,"success",refund);
        }
        try {
            weChatMiniAppClient.queryRefundStatus(refund,apiType,refund.getiAppId());
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage());
            throw e;
        }
        log.info("=== {} 成功",functionDescription);
        return new ResultObject<>(200,"success",refund);

    }
}
