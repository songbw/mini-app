package com.fengchao.miniapp.constant;

public class MyErrorCode {

    public static final String MOBILE_PATTEN = "^((13[0-9])|(145,147)|(15[0-9])|(18[0-9]))\\d{8}$";

    public static final String COMMON_PARAM_SHOW = " 入参: ";
    public static final String COMMON_PARAM_NULL = "@410001@入参未空 ";

    public static final String MYSQL_OPERATE_EXCEPTION = "@400001@数据库操作异常 ";
    public static final String MYSQL_INSERT_FAILED = "@400002@插入数据库失败 ";
    public static final String MYSQL_DELETE_FAILED = "@400003@数据库删除记录失败 ";
    public static final String MYSQL_UPDATE_FAILED = "@400004@更新数据库失败 ";
    public static final String MYSQL_SELECT_FAILED = "@400005@查询数据库失败 ";
    public static final String MYSQL_INSERT_SUCCESS = "插入数据库成功 id = ";
    public static final String MYSQL_UPDATE_SUCCESS = "更新数据库成功 id= ";
    public static final String MYSQL_DELETE_SUCCESS = "数据库删除记录成功 id = ";
    public static final String MYSQL_SELECT_SUCCESS = "查询数据库成功 : ";
    public static final String MYSQL_SELECT_NOT_FOUND = "@400007@查询数据库未找到记录 ";

    public static final String WECHAT_API_FAILED = "@400010@微信小程序接口失败 ";
    public static final String WECHAT_API_RESP_CODE_MISSING = "@400011@微信小程序接口返回码缺失 ";
    public static final String WECHAT_API_RESP_MSG_MISSING = "@400012@微信小程序接口返回信息缺失 ";
    public static final String WECHAT_API_RESP_MSG_WRONG = "@400013@微信小程序接口返回信息异常 ";
    public static final String WECHAT_API_OPEN_ID_BLANK = "@400110@微信小程序OpenId不可为空 ";
    public static final String WECHAT_API_OPEN_ID_WRONG = "@400111@微信小程序OpenId格式错误 ";

    public static final String ID_BLANK = "@400021@身份证号不能为空";
    public static final String ID_WRONG_FORMAT = "@400022@身份证号格式错误";
    public static final String ID_VALID_DATE_BLANK = "@400023@身份证有效期不能为空";
    public static final String ID_VALID_DATE_WRONG = "@400024@身份证有效期格式错误,正确格式为:YYYYMMDD,例如，20191030";
    public static final String ID_FRONT_PHOTO_PATH_BLANK = "@400025@身份证正面照路径不能为空";
    public static final String ID_BACK_PHOTO_PATH_BLANK = "@400026@身份证背面照路径不能为空";
    public static final String PHONE_NUMBER_BLANK = "@400027@电话号码不能为空";
    public static final String PHONE_NUMBER_WRONG = "@400028@电话号码格式错误";
    public static final String OPEN_ID_BLANK = "@400029@用户ID不能为空";

    public static final String STORE_NAME_BLANK = "@400031@商户名称不能为空";
    public static final String PHOTO_PATH_BLANK_STORE_NUMBER = "@400033@店铺门牌照片路径不能为空";
    public static final String PHOTO_PATH_BLANK_STORE_INSIDE = "@400032@店铺内照片路径不能为空";
    public static final String LICENSE_PHOTO_PATH_BLANK = "@400034@营业执照照片不能为空";
    public static final String LICENSE_NUMBER_BLANK = "@400035@营业执照号不能为空";
    public static final String LICENSE_VALID_DATE_BLANK = "@400036@营业执照有效日期不能为空";
    public static final String LICENSE_VALID_DATE_WRONG = "@400037@营业执照有效日期格式错误,正确格式为:YYYYMMDD,例如，20191030";

    public static final String MERCHANT_NAME_BLANK = "@400041@商户名称不能为空";
    public static final String CORPORATION_NAME_BLANK = "@400042@法人姓名不能为空";
    public static final String OPERATOR_NAME_BLANK = "@400043@经办人姓名不能为空";
    public static final String CONTROLLER_NAME_BLANK = "@400044@实际控制人姓名不能为空";
    public static final String ADDRESS_BLANK = "@400045@地址不能为空";
    public static final String MERCHANT_TYPE_BLANK = "@400046@商户企业类型不能为空";
    public static final String MERCHANT_TYPE_WRONG = "@400047@商户企业类型错误";
    public static final String MERCHANT_ID_BLANK = "@400048@商户ID缺失";

    public static final String MERCHANT_REGISTER_FAILED = "@400141@商户未进件或进件未成功";
    public static final String MERCHANT_BIND_CARD_FAILED = "@400142@商户未绑卡";
    public static final String MERCHANT_INFO_MISSING = "@400143@二级商户企业信息缺失";


    public static final String BANK_CARD_NUMBER_BLANK = "@400051@银行卡号不能为空";
    public static final String BANK_CARD_FORMAT_WRONG = "@400052@银行卡号格式错误";
    public static final String USERINFO_TYPE_BLANK_BANK = "@400053@银行用户信息类型不能为空";
    public static final String USERINFO_TYPE_WRONG_BANK = "@400054@银行用户信息类型错误";
    public static final String BANK_INFO_MISSING = "@400055@对公用户信息不能缺失银行名,代码,地址信息";
    public static final String BANK_PHONE_BLANK = "@400056@对私用户信息不能缺失预留手机号";
    public static final String USERINFO_EXISTED = "@400057@用户信息已经存在 ";
    public static final String USERINFO_NAME_BLANK = "@400058@用户信息名缺失 ";
    public static final String CREDIT_BLANK = "@4000150@信用卡号缺失 ";
    public static final String CREDIT_VALID_DATE_BLANK = "@4000151@信用卡有效日期缺失 ";
    public static final String CREDIT_VALID_DATE_WRONG = "@4000152@信用卡有效日期格式错误 ";
    public static final String CREDIT_VALID_CODE_BLANK = "@4000153@信用卡校验码缺失 ";
    public static final String CREDIT_VALID_CODE_WRONG = "@4000154@信用卡校验码格式错误 ";
    public static final String USERINFO_ID_BLANK = "@400155@用户信息ID缺失 ";
    public static final String USERINFO_DATA_BLANK = "@400156@用户信息信息缺失 ";

}
