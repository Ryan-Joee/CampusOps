package com.campusops.controller;

import com.campusops.common.result.ApiResponse;
import com.campusops.service.TicketCategoryService;
import com.campusops.vo.ticket.TicketCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ticket-categories")
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryService ticketCategoryService;

    @GetMapping
    public ApiResponse<List<TicketCategoryVO>> list() {
        return ApiResponse.success(ticketCategoryService.listEnabled());
    }
}
