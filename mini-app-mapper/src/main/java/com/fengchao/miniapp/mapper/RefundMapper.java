package com.fengchao.miniapp.mapper;

import com.fengchao.miniapp.model.Refund;
import com.fengchao.miniapp.model.RefundExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;


@Mapper
@Component(value = "RefundMapper")
public interface RefundMapper {
    long countByExample(RefundExample example);

    int deleteByExample(RefundExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Refund record);

    int insertSelective(Refund record);

    List<Refund> selectByExample(RefundExample example);

    Refund selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Refund record, @Param("example") RefundExample example);

    int updateByExample(@Param("record") Refund record, @Param("example") RefundExample example);

    int updateByPrimaryKeySelective(Refund record);

    int updateByPrimaryKey(Refund record);
}
