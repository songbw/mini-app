package com.fengchao.miniapp.mapper;

import com.fengchao.miniapp.model.AliPayConfig;
import com.fengchao.miniapp.model.AliPayConfigExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface AliPayConfigMapper {
    long countByExample(AliPayConfigExample example);

    int deleteByExample(AliPayConfigExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AliPayConfig record);

    int insertSelective(AliPayConfig record);

    List<AliPayConfig> selectByExample(AliPayConfigExample example);

    AliPayConfig selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AliPayConfig record, @Param("example") AliPayConfigExample example);

    int updateByExample(@Param("record") AliPayConfig record, @Param("example") AliPayConfigExample example);

    int updateByPrimaryKeySelective(AliPayConfig record);

    int updateByPrimaryKey(AliPayConfig record);
}
