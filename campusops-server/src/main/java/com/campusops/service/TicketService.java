package com.campusops.service;

import com.campusops.common.page.PageResult;
import com.campusops.dto.ticket.TicketCreateRequest;
import com.campusops.dto.ticket.TicketQueryRequest;
import com.campusops.vo.ticket.TicketCreateResponse;
import com.campusops.vo.ticket.TicketDetailVO;
import com.campusops.vo.ticket.TicketListItemVO;

/**
 * 工单服务接口。
 */
public interface TicketService {

    TicketCreateResponse createTicket(Long userId, TicketCreateRequest request);

    PageResult<TicketListItemVO> listTickets(Long userId, TicketQueryRequest request);

    TicketDetailVO getTicketDetail(Long userId, Long ticketId);
}