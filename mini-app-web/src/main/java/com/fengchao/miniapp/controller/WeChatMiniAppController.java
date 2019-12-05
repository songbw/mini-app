package com.fengchao.miniapp.controller;


import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.constant.ApiType;
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
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
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

    @Autowired
    public WeChatMiniAppController(RefundServiceImpl refundService,PaymentServiceImpl paymentService,UserInfoServiceImpl userInfoServic,RedisDAO redisDAO,WeChatMiniAppClientImpl weChatMiniAppClient){

        this.weChatMiniAppClient = weChatMiniAppClient;
        this.redisDAO = redisDAO;
        this.userInfoService = userInfoServic;
        this.paymentService = paymentService;
        this.refundService = refundService;

    }

    @ApiOperation(value = "查询UserInfo", notes="查询UserInfo")
    @GetMapping("/token/{apiType}")
    public ResultObject<String>
    getToken(@ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType
            )throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} enter",_func);
        ResultObject<String> result = new ResultObject<>(200,"success",null);

        String storedToken = redisDAO.getWeChatToken();
        if (null != storedToken){

            result.setData(storedToken);

            log.info("=== {} 成功 got stored token {}",_func,storedToken);
            return result;
        }

        WeChatTokenResultBean bean;
        try{
            bean = weChatMiniAppClient.getAccessToken(apiType);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            throw new Exception(e);
        }

        if (null == bean.getAccess_token()){
            String m = MyErrorCode.WECHAT_API_TOKEN_NULL;
            log.error("{} {}",_func,m);
            throw new Exception(m);
        }

        String token = bean.getAccess_token();
        result.setData(token);

        redisDAO.storeWeChatToken(token,bean.getExpires_in());

        log.info("=== {} 成功 token = {} ",_func,token);
        return result;

    }

    @ApiOperation(value = "获取session", notes="获取session")
    @GetMapping("/login/{apiType}")
    public ResultObject<WeChatSessionResultBean>
    getSession(@ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
                @RequestParam  @Valid @NotBlank(message=MyErrorCode.WECHAT_API_JS_CODE_BLANK) String jsCode
            ) throws Exception{

        String _func = "login";
        if (!ApiType.isValidCode(apiType)){
            log.error("{} 不支持的API类型 {}",_func,apiType);
            return new ResultObject<>(400,"不支持的API类型:"+apiType,null);
        }
        log.info("==={} 参数 jsCode={}",_func,jsCode);
        ResultObject<WeChatSessionResultBean> result = new ResultObject<>(200,"success",null);

        WeChatSessionResultBean bean;
        try{
            bean = weChatMiniAppClient.getSession(jsCode,apiType);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            throw e;
        }

        UserInfo info ;
        try{
            info = userInfoService.selectByOpenId(bean.getOpenid());
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            throw e;
        }

        bean.setIsNewUser((null == info)?"1":"0");//1:是新用户 0:是老用户
        result.setData(bean);
        log.info("==={} 成功 {}",_func,JSON.toJSONString(bean));
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
        String _func = apiType+"统一下单 ";
        log.info("=== {} 入参 {}",_func,JSON.toJSONString(data));
        ResultObject<WechatPrepayBean> result = new ResultObject<>(200,"success",null);
        String ip = request.getRemoteAddr();

        WechatPrepayBean bean;
        try{
            bean = weChatMiniAppClient.postPrepayId(data,ip,apiType);
        }catch (Exception e){
            log.error("{} 失败 {}",_func,e.getMessage());
            throw e;
        }

        Payment payment = new Payment();
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

        log.info("=== {} 成功 {}",_func,JSON.toJSONString(bean));
        return result;

    }

    @ApiOperation(value = "支付结果通知", notes="支付结果通知")
    @PostMapping("/payment/notify")
    public String
    paymentNotify(@RequestBody  String xmlStr)
            throws Exception{

        String _func = "支付结果通知 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: {}",_func,xmlStr);
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
                log.error("{} {}",_func,MyErrorCode.WECHAT_NOTIFY_SIGN_WRONG);
                return failXml;
            }
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            return failXml;
        }

        Object respCodeObj = postMap.get(WeChat.RETURN_CODE_KEY);
        Object respMsgObj = postMap.get(WeChat.RETURN_MESSAGE_KEY);
        if (null == respCodeObj || respCodeObj.toString().isEmpty()){
            log.error("{} {} {}",_func, MyErrorCode.WECHAT_NOTIFY_ERROR, WeChat.RETURN_CODE_KEY);
            return failXml;
        }
        String respCode = respCodeObj.toString();
        if (!WeChat.RETURN_CODE_SUCCESS.equals(respCode)){
            String msg = (null==respMsgObj)?" ":respMsgObj.toString();
            log.error("{} {} {}",_func,MyErrorCode.WECHAT_NOTIFY_ERROR,msg);
            return failXml;
        }

        Object orderIdObj = postMap.get(WeChat.OUT_TRADE_NO_KEY);
        if(null == orderIdObj || orderIdObj.toString().isEmpty()){
            log.error("{} {}  商户订单号",_func,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        Object tranIdObj = postMap.get(WeChat.RESP_TRANSACTION_ID_KEY);
        Object timeEndObj = postMap.get(WeChat.RESP_TIME_END_KEY);
        Object cashFeeObj = postMap.get(WeChat.RESP_CASH_FEE_KEY);
        Object totalFeeObj = postMap.get(WeChat.TOTAL_FEE_KEY);
        Object resultObj = postMap.get(WeChat.RESULT_CODE_KEY);
        Object openIdObj = postMap.get(WeChat.OPEN_ID_KEY);

        if (null == tranIdObj || null == timeEndObj || null == cashFeeObj
            || null == totalFeeObj || null == resultObj || null == openIdObj
            ||tranIdObj.toString().isEmpty() || timeEndObj.toString().isEmpty()
            ||cashFeeObj.toString().isEmpty() || totalFeeObj.toString().isEmpty()
            || resultObj.toString().isEmpty()
            || openIdObj.toString().isEmpty()){

            log.error("{} {} 参数缺失",_func,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        String orderId = orderIdObj.toString();
        PageInfo<Payment> pages ;
        Payment payment ;
        try{
            pages = paymentService.queryList(1,1,"id","DESC",
                    openIdObj.toString(),orderId,null,null);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            throw e;
        }

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",_func,MyErrorCode.PAYMENT_NO_FOUND);
            return okXml;
        }

        payment = pages.getRows().get(0);
        if (PaymentStatusType.OK.getCode().equals(payment.getStatus())
                || PaymentStatusType.FAILED.getCode().equals(payment.getStatus())){

            log.info("{} 支付结果已经记录,不再重复处理",_func);
            return okXml;
        }

        Integer respTotalFee = Integer.valueOf(totalFeeObj.toString());
        if (!respTotalFee.equals(payment.getTotalFee())){
            log.error("{} 订单金额是与商户侧的订单金额不一致",_func);
            return failXml;
        }

        payment.setResult(resultObj.toString());
        payment.setRespTotalFee(respTotalFee);
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
        payment.setComments("收到支付结果通知");
        try{
            paymentService.update(payment);
        }catch (Exception e){
            log.error("{} 更新记录失败 {}",_func,e.getMessage());
        }

        log.info("=== {} exit 更新记录成功",_func);

        return okXml;
    }

    @ApiOperation(value = "支付成功更新订单", notes="支付成功更新订单")
    @PostMapping("/payment/status/{apiType}")
    public ResultObject<String>
    updatePayment(@ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
            @RequestBody  @Valid WechatPaymentQueryBean data)
            throws Exception{

        String _func = "支付成功更新订单状态 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: {}",_func, JSON.toJSON(data));

        String orderId = data.getOrderId();
        String openId = data.getOpenId();

        PageInfo<Payment>  pages = paymentService.queryList(1,1,"id","DESC",
                openId,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",_func, MyErrorCode.PAYMENT_NO_FOUND);
            throw new Exception(MyErrorCode.PAYMENT_NO_FOUND);
        }

        Payment payment = pages.getRows().get(0);
        if (PaymentStatusType.PREPAY.getCode().equals(payment.getStatus())){
            log.info("{} 需要调用微信接口查询订单状态");
            try {
                weChatMiniAppClient.queryPayment(payment,apiType);
            }catch (Exception e){
                log.error("{} {}",_func,e.getMessage());
                throw e;
            }
            //Todo 更新订单状态
        }

        log.info("=== {} 成功",_func);
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
        String _func = apiType + "关闭订单 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: {}",_func, JSON.toJSON(data));

        String orderId = data.getOrderId();
        String openId = data.getOpenId();

        PageInfo<Payment>  pages = paymentService.queryList(1,1,"id","DESC",
                openId,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",_func, MyErrorCode.PAYMENT_NO_FOUND);
            throw new Exception(MyErrorCode.PAYMENT_NO_FOUND);
        }

        Payment payment = pages.getRows().get(0);

        boolean isOk ;
        isOk = weChatMiniAppClient.closePayment(payment,apiType);
        if (!isOk){
            log.error("{} 失败",_func);
            return new ResultObject<>(400,"关闭订单失败","");
        }

        payment.setStatus(PaymentStatusType.CLOSED.getCode());
        payment.setComments("订单已经关闭");
        try {
            paymentService.update(payment);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            return new ResultObject<>(200,"关闭订单成功，更新支付记录状态失败","");
        }
        log.info("=== {} 成功",_func);
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
        String _func = apiType+"查询订单状态 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: orderId={}",_func, orderId);

        PageInfo<Payment>  pages = paymentService.queryList(1,1,"id","DESC",
                openId,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",_func,MyErrorCode.REFUND_NO_FOUND);
            throw new Exception(MyErrorCode.REFUND_NO_FOUND);
        }

        Payment payment = pages.getRows().get(0);

        if (PaymentStatusType.PREPAY.getCode().equals(payment.getStatus())){
            log.info("{} 需要调用微信接口查询订单状态");
        }else{
            log.info("=== {} 成功找到记录 {}",_func, JSON.toJSONString(payment));
            return new ResultObject<>(200,"success",payment);
        }

        try {
            weChatMiniAppClient.queryPayment(payment,apiType);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            throw e;
        }
        log.info("=== {} 成功",_func);
        return new ResultObject<>(200,"success",payment);

    }

    @ApiOperation(value = "申请退款", notes="申请退款")
    @PostMapping("/refund/{apiType}")
    public ResultObject<WechatRefundRespBean>
    postRefund(
               @ApiParam(value="apiType",required=true)  @PathVariable("apiType")  String apiType,
               @RequestBody  @Valid WechatRefundPostBean data)
            throws Exception{

        if (!ApiType.isValidCode(apiType)){
            log.error("不支持的API类型 {}",apiType);
            return new ResultObject<>(400,"不支持的API类型:"+apiType,null);
        }

        String _func = apiType+"申请退款 ";
        log.info("=== {} 入参 {}",_func,JSON.toJSONString(data));
        ResultObject<WechatRefundRespBean> result = new ResultObject<>(200,"success",null);

        String orderId = data.getOrderId();
        String openId = data.getOpenId();
        PageInfo<Payment>  pages = paymentService.queryList(1,1,"id","DESC",
                openId,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            String m = MyErrorCode.PAYMENT_NO_FOUND;
            log.error("{} {}",_func,m);
            throw new Exception(m);
        }

        Payment payment = pages.getRows().get(0);
        Integer paymentStatus = payment.getStatus();
        if (!PaymentStatusType.OK.getCode().equals(paymentStatus)){
            String msg = MyErrorCode.PAYMENT_FAILED;
            log.error("{} {}",_func,msg);
            throw new Exception(msg);
        }

        Refund refund;
        try {
            refund = weChatMiniAppClient.postRefund(data,apiType);
        }catch (Exception e){
            log.error("{} {}", _func,e.getMessage());
            throw e;
        }

        String refundStatus = refund.getStatus();
        if (WeChat.RETURN_CODE_SUCCESS.equals(refundStatus)) {
            refund.setComments("申请退款成功,等待结果中");
            try {
                refundService.insert(refund);
            } catch (Exception e) {
                log.error("{} 退款记录插入失败 {}", _func, e.getMessage());
            }
        }
        WechatRefundRespBean bean = new WechatRefundRespBean();
        bean.setRefundNo(refund.getRefundNo());
        bean.setWechatRefundNo(refund.getWechatRefundNo());
        bean.setRefundFee(refund.getRespRefundFee());
        bean.setCode(refund.getStatus());
        bean.setMsg(refund.getComments());

        result.setData(bean);

        log.info("=== {}  {}",_func, refund.getStatus());
        return result;

    }

    @ApiOperation(value = "退款结果通知", notes="退款结果通知")
    @PostMapping("/refund/notify")
    public String
    refundNotify(@RequestBody  String xmlStr)
            throws Exception{

        String _func = "退款结果通知 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: {}",_func,xmlStr);

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
            log.error("{} {}",_func,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }
        String respCode = respCodeObj.toString();
        if (!WeChat.RETURN_CODE_SUCCESS.equals(respCode)){
            String msg = (null==respMsgObj)?" ":respMsgObj.toString();
            log.error("{} {} {}",_func, MyErrorCode.WECHAT_NOTIFY_ERROR,msg);
            return failXml;
        }

        Object mchIdObj = postMap.get(WeChat.MERCHANT_ID_KEY);
        if(null == mchIdObj || null == mchIdObj.toString() ||mchIdObj.toString().isEmpty()){
            log.error("{} {} 商户号 缺失",_func,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }
        if (!WeChat.MINI_APP_PAYMENT_MCH_ID.equals(mchIdObj.toString())){
            log.error("{} {} 商户号 错误",_func,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        Object reqInfoObj = postMap.get(WeChat.NOTIFY_REQ_INFO_KEY);
        if (null == reqInfoObj || null == reqInfoObj.toString() || reqInfoObj.toString().isEmpty()){
            log.error("{} {} 加密信息 错误",_func,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        String reqInfo;
        try {
            reqInfo = weChatMiniAppClient.DecodePkcs7(reqInfoObj.toString());
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            return failXml;
        }
        if (null == reqInfo){
            log.error("{} 解密失败",_func);
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

            log.error("{} {} 参数缺失 ",_func,MyErrorCode.WECHAT_NOTIFY_ERROR);
            return failXml;
        }

        String orderId = orderIdObj.toString();
        String refundNo = outRefundNoObj.toString();
        PageInfo<Refund> pages ;
        Refund refund ;
        try{
            pages = refundService.queryList(1,1,"id","DESC",
                    null,refundNo,orderId,null,null);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            throw e;
        }

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",_func,MyErrorCode.REFUND_NO_FOUND);
            return okXml;
        }

        Integer settleRefundFee = Integer.valueOf(settleFeeObj.toString());
        Integer respRefundFee = Integer.valueOf(refundFeeObj.toString());
        refund = pages.getRows().get(0);
        if (!refund.getRefundRecvAccount().isEmpty() && !refund.getRefundAccount().isEmpty()){
            log.info("{} 退款结果通知已经记录,不再重复处理",_func);
            return okXml;
        }

        if (!refund.getTransactionId().equals(tranIdObj.toString())){
            log.error("{} {}: {} 与退款记录中 {} 不匹配",
                    _func,WeChat.RESP_TRANSACTION_ID_KEY,tranIdObj.toString(),refund.getTransactionId());
            return failXml;
        }

        if (!respRefundFee.equals(refund.getRefundFee())){
            log.warn("{} {}: {} 与退款记录中 {} 不相等",
                    _func,WeChat.REFUND_FEE_KEY,respRefundFee,refund.getRefundFee());

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
            log.error("{} 更新退款记录失败 {}",_func,e.getMessage());
        }

        log.info("=== {} exit 更新退款记录成功",_func);
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
        String _func = apiType+"查询退款结果 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: openId={} orderId={}",_func, openId, orderId);

        PageInfo<Refund>  pages = refundService.queryList(1,1,"id","DESC",
                    openId,null,orderId,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            String m = MyErrorCode.REFUND_NO_FOUND;
            log.error("{} {}",_func,m);
            throw new Exception(m);
        }

        Refund refund = pages.getRows().get(0);
        WechatRefundListBean refundListBean = new WechatRefundListBean();
        refundListBean.setOrderId(refund.getOrderId());
        refundListBean.setTransactionId(refund.getTransactionId());
        refundListBean.setTotalFee(refund.getTotalFee());
        refundListBean.setCashFee(refund.getCashFee());
        try {
            weChatMiniAppClient.queryRefund(refundListBean,apiType);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            throw e;
        }
        log.info("=== {} 成功",_func);
        return new ResultObject<>(200,"success",refundListBean);

    }

    @ApiOperation(value = "查询退款状态", notes="查询退款状态")
    @GetMapping("/refund/{refundNo}")
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
        String _func = "查询退款状态 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("=== {} 入参: openId={} refundNo={}",_func, openId, refundNo);

        PageInfo<Refund>  pages = refundService.queryList(1,1,"id","DESC",
                openId,refundNo,null,null,null);

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            String m = MyErrorCode.REFUND_NO_FOUND;
            log.error("{} {}",_func,m);
            throw new Exception(m);
        }

        Refund refund = pages.getRows().get(0);
        if (!refund.getRefundRecvAccount().isEmpty() && !refund.getRefundAccount().isEmpty()){
            log.info("{} 退款结果通知已经记录,不再重复处理",_func);
            return new ResultObject<>(200,"success",refund);
        }
        try {
            weChatMiniAppClient.queryRefundStatus(refund,apiType);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
            throw e;
        }
        log.info("=== {} 成功",_func);
        return new ResultObject<>(200,"success",refund);

    }
}
