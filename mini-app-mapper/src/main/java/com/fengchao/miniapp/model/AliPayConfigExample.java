package com.fengchao.miniapp.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AliPayConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public AliPayConfigExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIAppIdIsNull() {
            addCriterion("i_app_id is null");
            return (Criteria) this;
        }

        public Criteria andIAppIdIsNotNull() {
            addCriterion("i_app_id is not null");
            return (Criteria) this;
        }

        public Criteria andIAppIdEqualTo(String value) {
            addCriterion("i_app_id =", value, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdNotEqualTo(String value) {
            addCriterion("i_app_id <>", value, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdGreaterThan(String value) {
            addCriterion("i_app_id >", value, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdGreaterThanOrEqualTo(String value) {
            addCriterion("i_app_id >=", value, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdLessThan(String value) {
            addCriterion("i_app_id <", value, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdLessThanOrEqualTo(String value) {
            addCriterion("i_app_id <=", value, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdLike(String value) {
            addCriterion("i_app_id like", value, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdNotLike(String value) {
            addCriterion("i_app_id not like", value, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdIn(List<String> values) {
            addCriterion("i_app_id in", values, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdNotIn(List<String> values) {
            addCriterion("i_app_id not in", values, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdBetween(String value1, String value2) {
            addCriterion("i_app_id between", value1, value2, "iAppId");
            return (Criteria) this;
        }

        public Criteria andIAppIdNotBetween(String value1, String value2) {
            addCriterion("i_app_id not between", value1, value2, "iAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdIsNull() {
            addCriterion("pay_app_id is null");
            return (Criteria) this;
        }

        public Criteria andPayAppIdIsNotNull() {
            addCriterion("pay_app_id is not null");
            return (Criteria) this;
        }

        public Criteria andPayAppIdEqualTo(String value) {
            addCriterion("pay_app_id =", value, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdNotEqualTo(String value) {
            addCriterion("pay_app_id <>", value, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdGreaterThan(String value) {
            addCriterion("pay_app_id >", value, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdGreaterThanOrEqualTo(String value) {
            addCriterion("pay_app_id >=", value, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdLessThan(String value) {
            addCriterion("pay_app_id <", value, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdLessThanOrEqualTo(String value) {
            addCriterion("pay_app_id <=", value, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdLike(String value) {
            addCriterion("pay_app_id like", value, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdNotLike(String value) {
            addCriterion("pay_app_id not like", value, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdIn(List<String> values) {
            addCriterion("pay_app_id in", values, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdNotIn(List<String> values) {
            addCriterion("pay_app_id not in", values, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdBetween(String value1, String value2) {
            addCriterion("pay_app_id between", value1, value2, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayAppIdNotBetween(String value1, String value2) {
            addCriterion("pay_app_id not between", value1, value2, "payAppId");
            return (Criteria) this;
        }

        public Criteria andPayNotifyIsNull() {
            addCriterion("pay_notify is null");
            return (Criteria) this;
        }

        public Criteria andPayNotifyIsNotNull() {
            addCriterion("pay_notify is not null");
            return (Criteria) this;
        }

        public Criteria andPayNotifyEqualTo(String value) {
            addCriterion("pay_notify =", value, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyNotEqualTo(String value) {
            addCriterion("pay_notify <>", value, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyGreaterThan(String value) {
            addCriterion("pay_notify >", value, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyGreaterThanOrEqualTo(String value) {
            addCriterion("pay_notify >=", value, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyLessThan(String value) {
            addCriterion("pay_notify <", value, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyLessThanOrEqualTo(String value) {
            addCriterion("pay_notify <=", value, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyLike(String value) {
            addCriterion("pay_notify like", value, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyNotLike(String value) {
            addCriterion("pay_notify not like", value, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyIn(List<String> values) {
            addCriterion("pay_notify in", values, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyNotIn(List<String> values) {
            addCriterion("pay_notify not in", values, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyBetween(String value1, String value2) {
            addCriterion("pay_notify between", value1, value2, "payNotify");
            return (Criteria) this;
        }

        public Criteria andPayNotifyNotBetween(String value1, String value2) {
            addCriterion("pay_notify not between", value1, value2, "payNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyIsNull() {
            addCriterion("refund_notify is null");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyIsNotNull() {
            addCriterion("refund_notify is not null");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyEqualTo(String value) {
            addCriterion("refund_notify =", value, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyNotEqualTo(String value) {
            addCriterion("refund_notify <>", value, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyGreaterThan(String value) {
            addCriterion("refund_notify >", value, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyGreaterThanOrEqualTo(String value) {
            addCriterion("refund_notify >=", value, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyLessThan(String value) {
            addCriterion("refund_notify <", value, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyLessThanOrEqualTo(String value) {
            addCriterion("refund_notify <=", value, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyLike(String value) {
            addCriterion("refund_notify like", value, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyNotLike(String value) {
            addCriterion("refund_notify not like", value, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyIn(List<String> values) {
            addCriterion("refund_notify in", values, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyNotIn(List<String> values) {
            addCriterion("refund_notify not in", values, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyBetween(String value1, String value2) {
            addCriterion("refund_notify between", value1, value2, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andRefundNotifyNotBetween(String value1, String value2) {
            addCriterion("refund_notify not between", value1, value2, "refundNotify");
            return (Criteria) this;
        }

        public Criteria andApiUrlIsNull() {
            addCriterion("api_url is null");
            return (Criteria) this;
        }

        public Criteria andApiUrlIsNotNull() {
            addCriterion("api_url is not null");
            return (Criteria) this;
        }

        public Criteria andApiUrlEqualTo(String value) {
            addCriterion("api_url =", value, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlNotEqualTo(String value) {
            addCriterion("api_url <>", value, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlGreaterThan(String value) {
            addCriterion("api_url >", value, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlGreaterThanOrEqualTo(String value) {
            addCriterion("api_url >=", value, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlLessThan(String value) {
            addCriterion("api_url <", value, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlLessThanOrEqualTo(String value) {
            addCriterion("api_url <=", value, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlLike(String value) {
            addCriterion("api_url like", value, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlNotLike(String value) {
            addCriterion("api_url not like", value, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlIn(List<String> values) {
            addCriterion("api_url in", values, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlNotIn(List<String> values) {
            addCriterion("api_url not in", values, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlBetween(String value1, String value2) {
            addCriterion("api_url between", value1, value2, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andApiUrlNotBetween(String value1, String value2) {
            addCriterion("api_url not between", value1, value2, "apiUrl");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andCommentsIsNull() {
            addCriterion("comments is null");
            return (Criteria) this;
        }

        public Criteria andCommentsIsNotNull() {
            addCriterion("comments is not null");
            return (Criteria) this;
        }

        public Criteria andCommentsEqualTo(String value) {
            addCriterion("comments =", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsNotEqualTo(String value) {
            addCriterion("comments <>", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsGreaterThan(String value) {
            addCriterion("comments >", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsGreaterThanOrEqualTo(String value) {
            addCriterion("comments >=", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsLessThan(String value) {
            addCriterion("comments <", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsLessThanOrEqualTo(String value) {
            addCriterion("comments <=", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsLike(String value) {
            addCriterion("comments like", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsNotLike(String value) {
            addCriterion("comments not like", value, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsIn(List<String> values) {
            addCriterion("comments in", values, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsNotIn(List<String> values) {
            addCriterion("comments not in", values, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsBetween(String value1, String value2) {
            addCriterion("comments between", value1, value2, "comments");
            return (Criteria) this;
        }

        public Criteria andCommentsNotBetween(String value1, String value2) {
            addCriterion("comments not between", value1, value2, "comments");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyIsNull() {
            addCriterion("private_key is null");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyIsNotNull() {
            addCriterion("private_key is not null");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyEqualTo(String value) {
            addCriterion("private_key =", value, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyNotEqualTo(String value) {
            addCriterion("private_key <>", value, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyGreaterThan(String value) {
            addCriterion("private_key >", value, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyGreaterThanOrEqualTo(String value) {
            addCriterion("private_key >=", value, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyLessThan(String value) {
            addCriterion("private_key <", value, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyLessThanOrEqualTo(String value) {
            addCriterion("private_key <=", value, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyLike(String value) {
            addCriterion("private_key like", value, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyNotLike(String value) {
            addCriterion("private_key not like", value, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyIn(List<String> values) {
            addCriterion("private_key in", values, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyNotIn(List<String> values) {
            addCriterion("private_key not in", values, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyBetween(String value1, String value2) {
            addCriterion("private_key between", value1, value2, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPrivateKeyNotBetween(String value1, String value2) {
            addCriterion("private_key not between", value1, value2, "privateKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyIsNull() {
            addCriterion("public_key is null");
            return (Criteria) this;
        }

        public Criteria andPublicKeyIsNotNull() {
            addCriterion("public_key is not null");
            return (Criteria) this;
        }

        public Criteria andPublicKeyEqualTo(String value) {
            addCriterion("public_key =", value, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyNotEqualTo(String value) {
            addCriterion("public_key <>", value, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyGreaterThan(String value) {
            addCriterion("public_key >", value, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyGreaterThanOrEqualTo(String value) {
            addCriterion("public_key >=", value, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyLessThan(String value) {
            addCriterion("public_key <", value, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyLessThanOrEqualTo(String value) {
            addCriterion("public_key <=", value, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyLike(String value) {
            addCriterion("public_key like", value, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyNotLike(String value) {
            addCriterion("public_key not like", value, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyIn(List<String> values) {
            addCriterion("public_key in", values, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyNotIn(List<String> values) {
            addCriterion("public_key not in", values, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyBetween(String value1, String value2) {
            addCriterion("public_key between", value1, value2, "publicKey");
            return (Criteria) this;
        }

        public Criteria andPublicKeyNotBetween(String value1, String value2) {
            addCriterion("public_key not between", value1, value2, "publicKey");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}