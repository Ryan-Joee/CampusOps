package com.campusops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusops.entity.TicketCategoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ticket_category 表 Mapper。
 */
@Mapper
public interface TicketCategoryMapper extends BaseMapper<TicketCategoryEntity> {

    TicketCategoryEntity selectEnabledById(@Param("id") Long id);
}