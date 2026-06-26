package com.campusops.vo.ticket;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketCategoryVO {

    private Long id;
    private String categoryCode;
    private String categoryName;
    private String description;
}
