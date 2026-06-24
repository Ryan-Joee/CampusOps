package com.campusops.vo.ticket;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建工单响应 VO。
 */
@Data
@Builder
public class TicketCreateResponse {

    private Long id;
    private String ticketNo;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}