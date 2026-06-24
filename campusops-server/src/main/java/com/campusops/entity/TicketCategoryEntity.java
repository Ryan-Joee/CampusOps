package com.campusops.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对应 ticket_category 表。
 */
@Data
@TableName("ticket_category")
public class TicketCategoryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String categoryCode;
    private String categoryName;
    private String description;
    private Long parentId;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}