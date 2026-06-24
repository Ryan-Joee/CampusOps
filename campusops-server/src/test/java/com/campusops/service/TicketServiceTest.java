package com.campusops.service;

import com.campusops.service.impl.TicketServiceImpl;
import com.campusops.dto.ticket.TicketCreateRequest;
import com.campusops.dto.ticket.TicketQueryRequest;
import com.campusops.vo.ticket.TicketCreateResponse;
import com.campusops.vo.ticket.TicketDetailVO;
import com.campusops.vo.ticket.TicketListItemVO;
import com.campusops.common.exception.BusinessException;
import com.campusops.common.page.PageResult;
import com.campusops.entity.TicketCategoryEntity;
import com.campusops.entity.TicketEntity;
import com.campusops.mapper.TicketCategoryMapper;
import com.campusops.mapper.TicketMapper;
import com.campusops.security.jwt.LoginUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketMapper ticketMapper;

    @Mock
    private TicketCategoryMapper ticketCategoryMapper;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private LoginUserPrincipal studentPrincipal;
    private LoginUserPrincipal techPrincipal;
    private LoginUserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        studentPrincipal = new LoginUserPrincipal(5L, "student001", List.of("normal_user"));
        techPrincipal = new LoginUserPrincipal(3L, "tech_network", List.of("technician"));
        adminPrincipal = new LoginUserPrincipal(2L, "service_admin", List.of("service_admin"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setPrincipal(LoginUserPrincipal p) {
        var auth = new UsernamePasswordAuthenticationToken(p, "", p.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ---- 创建工单 ----

    @Test
    void createTicket_shouldReturnPendingAssignment() {
        setPrincipal(studentPrincipal);
        TicketCreateRequest req = new TicketCreateRequest();
        req.setTitle("网络故障");
        req.setDescription("校园网无法连接");
        req.setPriority("high");

        when(ticketMapper.insert(any(TicketEntity.class))).thenAnswer(inv -> {
            TicketEntity e = inv.getArgument(0);
            e.setId(100L);
            return 1;
        });
        when(ticketMapper.updateById(any(TicketEntity.class))).thenReturn(1);

        TicketCreateResponse resp = ticketService.createTicket(5L, req);

        assertThat(resp.getStatus()).isEqualTo("pending_assignment");
        assertThat(resp.getTicketNo()).startsWith("INC-");
    }

    @Test
    void createTicket_invalidPriority_shouldThrow() {
        setPrincipal(studentPrincipal);
        TicketCreateRequest req = new TicketCreateRequest();
        req.setTitle("title");
        req.setDescription("desc");
        req.setPriority("critical"); // 非法

        assertThatThrownBy(() -> ticketService.createTicket(5L, req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("INVALID_TICKET_PRIORITY");
    }

    @Test
    void createTicket_categoryNotFound_shouldThrow() {
        setPrincipal(studentPrincipal);
        TicketCreateRequest req = new TicketCreateRequest();
        req.setTitle("title");
        req.setDescription("desc");
        req.setCategoryId(999L);

        when(ticketCategoryMapper.selectEnabledById(999L)).thenReturn(null);

        assertThatThrownBy(() -> ticketService.createTicket(5L, req))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("CATEGORY_NOT_FOUND");
    }

    // ---- 详情权限 ----

    @Test
    void normalUser_cannotViewOtherTicket() {
        setPrincipal(studentPrincipal);
        TicketEntity other = ticketEntity(1L, "INC-2026-0001", 6L, 3L); // submitter_id=6
        when(ticketMapper.selectTicketDetail(1L)).thenReturn(other);

        assertThatThrownBy(() -> ticketService.getTicketDetail(5L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("TICKET_NOT_FOUND");
    }

    @Test
    void normalUser_canViewOwnTicket() {
        setPrincipal(studentPrincipal);
        TicketEntity own = ticketEntity(1L, "INC-2026-0001", 5L, null);
        when(ticketMapper.selectTicketDetail(1L)).thenReturn(own);

        TicketDetailVO vo = ticketService.getTicketDetail(5L, 1L);
        assertThat(vo.getId()).isEqualTo(1L);
    }

    @Test
    void technician_canViewAssignedTicket() {
        setPrincipal(techPrincipal);
        TicketEntity assigned = ticketEntity(2L, "INC-2026-0002", 6L, 3L); // assignee_id=3
        when(ticketMapper.selectTicketDetail(2L)).thenReturn(assigned);

        TicketDetailVO vo = ticketService.getTicketDetail(3L, 2L);
        assertThat(vo.getId()).isEqualTo(2L);
    }

    @Test
    void serviceAdmin_canViewAnyTicket() {
        setPrincipal(adminPrincipal);
        TicketEntity any = ticketEntity(3L, "INC-2026-0003", 6L, 4L);
        when(ticketMapper.selectTicketDetail(3L)).thenReturn(any);

        TicketDetailVO vo = ticketService.getTicketDetail(2L, 3L);
        assertThat(vo.getId()).isEqualTo(3L);
    }

    @Test
    void ticketNotFound_shouldThrow() {
        setPrincipal(adminPrincipal);
        when(ticketMapper.selectTicketDetail(999L)).thenReturn(null);

        assertThatThrownBy(() -> ticketService.getTicketDetail(2L, 999L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("TICKET_NOT_FOUND");
    }

    // ---- 列表 ----

    @Test
    void listTickets_shouldReturnPageResult() {
        setPrincipal(adminPrincipal);
        TicketQueryRequest req = new TicketQueryRequest();
        req.setPage(1);
        req.setPageSize(10);

        TicketEntity t1 = ticketEntity(1L, "INC-2026-0001", 5L, 3L);
        when(ticketMapper.selectTicketPage(any(), any(), any(), any(), any(), eq(0L), eq(10L)))
                .thenReturn(List.of(t1));
        when(ticketMapper.countTicketPage(any(), any(), any(), any(), any()))
                .thenReturn(1L);

        PageResult<TicketListItemVO> result = ticketService.listTickets(2L, req);

        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getTicketNo()).isEqualTo("INC-2026-0001");
    }

    // ---- helper ----

    private TicketEntity ticketEntity(Long id, String ticketNo, Long submitterId, Long assigneeId) {
        TicketEntity e = new TicketEntity();
        e.setId(id);
        e.setTicketNo(ticketNo);
        e.setTitle("测试工单");
        e.setDescription("描述");
        e.setPriority("medium");
        e.setStatus("pending_assignment");
        e.setSubmitterId(submitterId);
        e.setAssigneeId(assigneeId);
        e.setSource("web");
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        return e;
    }
}