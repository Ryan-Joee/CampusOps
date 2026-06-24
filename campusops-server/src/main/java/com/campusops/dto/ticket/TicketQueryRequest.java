package com.campusops.dto.ticket;

import lombok.Data;

/**
 * 工单列表查询请求 DTO。
 */
@Data
public class TicketQueryRequest {

    private String status;
    private Long categoryId;
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 10;

    public int getPage() {
        return page != null && page >= 1 ? page : 1;
    }

    public int getPageSize() {
        if (pageSize == null || pageSize < 1) return 10;
        return Math.min(pageSize, 100);
    }
}