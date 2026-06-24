package com.campusops.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对应 ticket 表。
 */
@Data
@TableName("ticket")
public class TicketEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketNo;
    private String title;
    private String description;
    private Long categoryId;
    private String priority;
    private String status;
    private Long submitterId;
    private Long assigneeId;
    private LocalDateTime assignedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime dueAt;
    private String source;
    private String aiSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}