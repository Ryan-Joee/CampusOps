package com.campusops.controller;

import com.campusops.common.page.PageResult;
import com.campusops.common.result.ApiResponse;
import com.campusops.dto.ticket.TicketCreateRequest;
import com.campusops.dto.ticket.TicketQueryRequest;
import com.campusops.security.jwt.LoginUserPrincipal;
import com.campusops.service.TicketService;
import com.campusops.vo.ticket.TicketCreateResponse;
import com.campusops.vo.ticket.TicketDetailVO;
import com.campusops.vo.ticket.TicketListItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ApiResponse<TicketCreateResponse> createTicket(
            @AuthenticationPrincipal LoginUserPrincipal principal,
            @Valid @RequestBody TicketCreateRequest request) {
        TicketCreateResponse response = ticketService.createTicket(principal.getUserId(), request);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<PageResult<TicketListItemVO>> listTickets(
            @AuthenticationPrincipal LoginUserPrincipal principal,
            TicketQueryRequest request) {
        PageResult<TicketListItemVO> result = ticketService.listTickets(principal.getUserId(), request);
        return ApiResponse.success(result);
    }

    @GetMapping("/{ticketId}")
    public ApiResponse<TicketDetailVO> getTicketDetail(
            @AuthenticationPrincipal LoginUserPrincipal principal,
            @PathVariable Long ticketId) {
        TicketDetailVO vo = ticketService.getTicketDetail(principal.getUserId(), ticketId);
        return ApiResponse.success(vo);
    }
}