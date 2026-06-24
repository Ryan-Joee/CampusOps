package com.campusops.vo.ticket;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单列表项 VO。
 */
@Data
public class TicketListItemVO {

    private Long id;
    private String ticketNo;
    private String title;
    private Long categoryId;
    private String categoryName;
    private String priority;
    private String status;
    private Long submitterId;
    private String submitterName;
    private Long assigneeId;
    private String assigneeName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}