package com.campusops.service.impl;

import com.campusops.entity.TicketCategoryEntity;
import com.campusops.mapper.TicketCategoryMapper;
import com.campusops.service.TicketCategoryService;
import com.campusops.vo.ticket.TicketCategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketCategoryServiceImpl implements TicketCategoryService {

    private final TicketCategoryMapper ticketCategoryMapper;

    @Override
    public List<TicketCategoryVO> listEnabled() {
        List<TicketCategoryEntity> list = ticketCategoryMapper.selectList(
                new LambdaQueryWrapper<TicketCategoryEntity>()
                        .eq(TicketCategoryEntity::getEnabled, 1)
                        .eq(TicketCategoryEntity::getDeleted, 0)
                        .orderByAsc(TicketCategoryEntity::getId)
        );
        return list.stream()
                .map(e -> TicketCategoryVO.builder()
                        .id(e.getId())
                        .categoryCode(e.getCategoryCode())
                        .categoryName(e.getCategoryName())
                        .description(e.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}
