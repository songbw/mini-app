package com.fengchao.miniapp.constant;

public class WeChat {

    public static final String MINI_APP_PAYMENT_API_KEY = "DC12C1282924ED35D5B4A1F98E0544E9";
    public static final String MINI_APP_PAYMENT_MCH_ID = "1553849241";
    public static final String MINI_APP_CERT_FILE = "cert/apiclient_cert.p12";

    public static final String MINI_APP_PAYMENT_UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    public static final String MINI_APP_PAYMENT_NOTIFY = "https://xcx-dev.weesharing.com/wechat/payment/notify";
    public static final String MINI_APP_PAYMENT_QUERY_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
    public static final String MINI_APP_PAYMENT_CLOSE_URL = "https://api.mch.weixin.qq.com/pay/closeorder";

    public static final String MINI_APP_REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
    public static final String MINI_APP_REFUND_NOTIFY = "https://xcx-dev.weesharing.com/wechat/refund/notify";
    public static final String MINI_APP_REFUND_QUERY_URL = "https://api.mch.weixin.qq.com/pay/refundquery";

    public static final String ERR_CODE_KEY = "errcode";
    public static final String ERR_MESSAGE_KEY = "errmsg";
    public static final String EXPIRES_IN_KEY = "expires_in";

    public static final String RESULT_CODE_SUCCESS = "SUCCESS";

    public static final String GET_ACCESS_TOKEN_PATH = "cgi-bin/token";
    public static final String GET_ACCESS_TOKEN_API = "auth.getAccessToken ";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String GRANT_TYPE_DEFAULT = "client_credential";
    public static final String APP_ID_KEY = "appid";
    public static final String SECRET_KEY = "secret";
    public static final String ACCESS_TOKEN_KEY = "access_token";

    public static final String GET_CODE2SESSION_PATH = "sns/jscode2session";
    public static final String JS_CODE_KEY = "js_code";
    public static final String GRANT_TYPE_AUTH_CODE = "authorization_code";
    public static final String OPEN_ID_KEY = "openid";
    public static final String SESSION_KEY_KEY = "session_key";
    public static final String UNION_ID_KEY = "unionid";

    public static final String MERCHANT_ID_KEY = "mch_id";
    public static final String NONCE_STRING_KEY = "nonce_str";
    public static final String SIGN_KEY = "sign";
    public static final String SIGN_TYPE_KEY = "sign_type";
    public static final String SIGN_TYPE_MD5 = "MD5";
    public static final String BODY_KEY = "body";
    public static final String OUT_TRADE_NO_KEY = "out_trade_no";
    public static final String TOTAL_FEE_KEY = "total_fee";
    public static final String SPBILL_CREATE_IP_KEY = "spbill_create_ip";//终端IP
    public static final String NOTIFY_URL_KEY = "notify_url";
    public static final String TRADE_TYPE_KEY = "trade_type";
    public static final String TRADE_TYPE_JSAPI = "JSAPI";
    public static final String PREPAY_ID_KEY = "prepay_id";
    public static final String TRADE_STATE_KEY = "trade_state";
    public static final String TRADE_STATE_DESC_KEY = "trade_state_desc";
    public static final String BANK_TYPE_KEY = "bank_type";

    public static final String RETURN_CODE_KEY = "return_code";
    public static final String RETURN_CODE_SUCCESS = "SUCCESS";
    public static final String RETURN_CODE_FAIL = "FAIL";
    public static final String RETURN_MESSAGE_KEY = "return_msg";
    public static final String RESULT_CODE_KEY = "result_code";
    public static final String RESULT_MESSAGE_KEY = "result_msg";
    public static final String RESP_ERR_CODE_KEY = "err_code";
    public static final String RESP_ERR_MESSAGE_KEY = "err_code_des";

    public static final String RESP_TRANSACTION_ID_KEY = "transaction_id";
    public static final String RESP_CASH_FEE_KEY = "cash_fee";
    public static final String CASH_REFUND_FEE_KEY = "cash_refund_fee";
    public static final String RESP_TIME_END_KEY = "time_end";
    public static final String OUT_REFUND_NO_KEY = "out_refund_no";
    public static final String REFUND_FEE_KEY = "refund_fee";//申请退款金额
    public static final String NOTIFY_REQ_INFO_KEY = "req_info";
    public static final String SUCCESS_TIME_KEY = "success_time";
    public static final String REFUND_ID_KEY = "refund_id";
    public static final String REFUND_COUNT_KEY = "refund_count";//退款笔数

    //退款金额,退款金额=申请退款金额-非充值代金券退款金额，退款金额<=申请退款金额
    public static final String SETTLE_REFUND_FEE_KEY = "settlement_refund_fee";
    public static final String REFUND_STATUS_KEY = "refund_status";
    //退款入账账户  example: 招商银行信用卡0403
    //取当前退款单的退款入账方
    //1）退回银行卡：
    //{银行名称}{卡类型}{卡尾号}
    //2）退回支付用户零钱:
    //支付用户零钱
    //3）退还商户:
    //商户基本账户
    //商户结算银行账户
    //4）退回支付用户零钱通:
    //支付用户零钱通
    public static final String RECV_ACCOUNT_KEY = "refund_recv_accout";

    //退款资金来源
    //REFUND_SOURCE_RECHARGE_FUNDS 可用余额退款/基本账户
    //REFUND_SOURCE_UNSETTLED_FUNDS 未结算资金退款
    public static final String REFUND_ACCOUNT_KEY = "refund_account";

    //退款发起来源
    //API接口
    //VENDOR_PLATFORM商户平台
    public static final String REFUND_REQUEST_SOURCE_KEY = "refund_request_source";

    public static final String REFUND_SUCCESS_TIME_KEY = "refund_success_time";

}
