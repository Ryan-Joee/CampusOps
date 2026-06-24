package com.campusops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusops.entity.TicketEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ticket 表 Mapper。
 */
@Mapper
public interface TicketMapper extends BaseMapper<TicketEntity> {

    List<TicketEntity> selectTicketPage(@Param("submitterIds") List<Long> submitterIds,
                                        @Param("assigneeId") Long assigneeId,
                                        @Param("status") String status,
                                        @Param("categoryId") Long categoryId,
                                        @Param("keyword") String keyword,
                                        @Param("offset") long offset,
                                        @Param("limit") long limit);

    long countTicketPage(@Param("submitterIds") List<Long> submitterIds,
                         @Param("assigneeId") Long assigneeId,
                         @Param("status") String status,
                         @Param("categoryId") Long categoryId,
                         @Param("keyword") String keyword);

    TicketEntity selectTicketDetail(@Param("id") Long id);
}