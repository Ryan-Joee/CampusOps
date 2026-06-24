package com.campusops.controller;

import com.campusops.common.page.PageResult;
import com.campusops.dto.ticket.TicketCreateRequest;
import com.campusops.entity.SysUserEntity;
import com.campusops.mapper.SysRoleMapper;
import com.campusops.mapper.SysUserMapper;
import com.campusops.mapper.TicketMapper;
import com.campusops.security.jwt.JwtProperties;
import com.campusops.security.jwt.JwtTokenProvider;
import com.campusops.service.TicketService;
import com.campusops.vo.ticket.TicketCreateResponse;
import com.campusops.vo.ticket.TicketDetailVO;
import com.campusops.vo.ticket.TicketListItemVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private SysUserMapper sysUserMapper;

    @MockitoBean
    private SysRoleMapper sysRoleMapper;

    private String userToken() {
        SysUserEntity mockUser = new SysUserEntity();
        mockUser.setId(5L);
        mockUser.setUsername("student001");
        mockUser.setStatus("enabled");
        when(sysUserMapper.selectEnabledById(5L)).thenReturn(mockUser);
        when(sysRoleMapper.selectRoleCodesByUserId(5L)).thenReturn(List.of("normal_user"));

        JwtProperties props = new JwtProperties();
        props.setSecret("change-this-dev-secret-change-this-dev-secret");
        props.setExpirationSeconds(7200);
        return new JwtTokenProvider(props).generateToken(5L, "student001", List.of("normal_user"));
    }

    @Test
    void createTicket_emptyBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken())
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTicket_validBody_shouldReturn200() throws Exception {
        TicketCreateRequest req = new TicketCreateRequest();
        req.setTitle("网络故障");
        req.setDescription("校园网无法连接");

        TicketCreateResponse resp = TicketCreateResponse.builder()
                .id(1L).ticketNo("INC-2026-0001").status("pending_assignment")
                .createdAt(LocalDateTime.now()).build();
        when(ticketService.createTicket(anyLong(), any())).thenReturn(resp);

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken())
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("pending_assignment"));
    }

    @Test
    void getTickets_shouldReturn200() throws Exception {
        PageResult<TicketListItemVO> empty = PageResult.empty(1, 10);
        when(ticketService.listTickets(anyLong(), any())).thenReturn(empty);

        mockMvc.perform(get("/tickets")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    void getTicketDetail_shouldReturn200() throws Exception {
        TicketDetailVO vo = new TicketDetailVO();
        vo.setId(1L);
        vo.setTicketNo("INC-2026-0001");
        when(ticketService.getTicketDetail(anyLong(), anyLong())).thenReturn(vo);

        mockMvc.perform(get("/tickets/1")
                        .header("Authorization", "Bearer " + userToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getTickets_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isUnauthorized());
    }
}