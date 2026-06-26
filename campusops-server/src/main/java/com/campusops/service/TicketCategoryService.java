package com.campusops.service;

import com.campusops.vo.ticket.TicketCategoryVO;

import java.util.List;

public interface TicketCategoryService {

    List<TicketCategoryVO> listEnabled();
}
