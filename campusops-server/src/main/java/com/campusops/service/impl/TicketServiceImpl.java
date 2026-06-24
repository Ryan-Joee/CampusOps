package com.campusops.service.impl;

import com.campusops.common.exception.BusinessException;
import com.campusops.common.page.PageResult;
import com.campusops.common.result.ResultCode;
import com.campusops.dto.ticket.TicketCreateRequest;
import com.campusops.dto.ticket.TicketQueryRequest;
import com.campusops.entity.TicketCategoryEntity;
import com.campusops.entity.TicketEntity;
import com.campusops.mapper.TicketCategoryMapper;
import com.campusops.mapper.TicketMapper;
import com.campusops.security.jwt.LoginUserPrincipal;
import com.campusops.service.TicketService;
import com.campusops.vo.ticket.TicketCreateResponse;
import com.campusops.vo.ticket.TicketDetailVO;
import com.campusops.vo.ticket.TicketListItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private static final Set<String> VALID_STATUSES = Set.of(
            "pending_assignment", "pending_process", "processing",
            "pending_confirm", "closed", "rejected", "canceled"
    );
    private static final Set<String> VALID_PRIORITIES = Set.of("low", "medium", "high", "urgent");
    private static final String ROLE_SERVICE_ADMIN = "service_admin";
    private static final String ROLE_TECHNICIAN = "technician";

    private final TicketMapper ticketMapper;
    private final TicketCategoryMapper ticketCategoryMapper;

    @Override
    @Transactional
    public TicketCreateResponse createTicket(Long userId, TicketCreateRequest request) {
        // 校验优先级
        String priority = request.getPriority();
        if (priority == null || priority.isBlank()) {
            priority = "medium";
        }
        if (!VALID_PRIORITIES.contains(priority)) {
            throw new BusinessException(ResultCode.INVALID_TICKET_PRIORITY, "优先级仅允许 low/medium/high/urgent");
        }

        // 校验分类
        if (request.getCategoryId() != null) {
            TicketCategoryEntity category = ticketCategoryMapper.selectEnabledById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND, "工单分类不存在或已禁用");
            }
        }

        TicketEntity entity = new TicketEntity();
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setCategoryId(request.getCategoryId());
        entity.setPriority(priority);
        entity.setStatus("pending_assignment");
        entity.setSubmitterId(userId);
        entity.setSource("web");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        ticketMapper.insert(entity);

        // 生成 ticket_no: INC-yyyy-xxxx
        String ticketNo = buildTicketNo(entity.getId());
        entity.setTicketNo(ticketNo);
        ticketMapper.updateById(entity);

        log.info("工单创建成功: id={}, ticketNo={}, submitterId={}", entity.getId(), ticketNo, userId);

        return TicketCreateResponse.builder()
                .id(entity.getId())
                .ticketNo(ticketNo)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    public PageResult<TicketListItemVO> listTickets(Long userId, TicketQueryRequest request) {
        int page = request.getPage();
        int pageSize = request.getPageSize();
        long offset = (long) (page - 1) * pageSize;

        LoginUserPrincipal principal = getCurrentPrincipal();
        List<Long> submitterIds = buildSubmitterIds(principal, userId);
        Long assigneeId = buildAssigneeId(principal, userId);

        List<TicketEntity> entities = ticketMapper.selectTicketPage(
                submitterIds, assigneeId,
                request.getStatus(), request.getCategoryId(),
                request.getKeyword(), offset, pageSize);

        long total = ticketMapper.countTicketPage(
                submitterIds, assigneeId,
                request.getStatus(), request.getCategoryId(),
                request.getKeyword());

        List<TicketListItemVO> items = entities.stream()
                .map(this::toListItemVO)
                .collect(Collectors.toList());

        return PageResult.of(items, page, pageSize, total);
    }

    @Override
    public TicketDetailVO getTicketDetail(Long userId, Long ticketId) {
        TicketEntity entity = ticketMapper.selectTicketDetail(ticketId);
        if (entity == null) {
            throw new BusinessException(ResultCode.TICKET_NOT_FOUND, "工单不存在或无权访问");
        }

        LoginUserPrincipal principal = getCurrentPrincipal();
        checkAccess(principal, entity);

        return toDetailVO(entity);
    }

    // ---- 权限判断 ----

    /**
     * 从 SecurityContext 获取当前登录用户。
     */
    private LoginUserPrincipal getCurrentPrincipal() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUserPrincipal)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return (LoginUserPrincipal) auth.getPrincipal();
    }

    /**
     * 构建列表查询的 submitterIds 范围：
     * - normal_user: 只有自己
     * - technician: 自己
     * - service_admin: null（不按提交人过滤）
     * - system_admin: 只按自己（除非有 service_admin/technician）
     */
    private List<Long> buildSubmitterIds(LoginUserPrincipal principal, Long userId) {
        if (hasRole(principal, ROLE_SERVICE_ADMIN)) {
            return null; // 不按 submitter 过滤
        }
        return List.of(userId);
    }

    /**
     * 构建列表查询的 assigneeId：
     * - technician: 如果同时不是 service_admin，加上自己的 assigneeId
     * - service_admin: null（不按处理人过滤）
     * - normal_user/system_admin: null
     */
    private Long buildAssigneeId(LoginUserPrincipal principal, Long userId) {
        if (hasRole(principal, ROLE_SERVICE_ADMIN)) {
            return null;
        }
        if (hasRole(principal, ROLE_TECHNICIAN)) {
            return userId;
        }
        return null;
    }

    /**
     * 检查当前用户是否有权访问该工单。
     */
    private void checkAccess(LoginUserPrincipal principal, TicketEntity entity) {
        if (hasRole(principal, ROLE_SERVICE_ADMIN)) {
            return;
        }

        Long currentUserId = principal.getUserId();

        // 提交人可以查看自己的
        if (currentUserId.equals(entity.getSubmitterId())) {
            return;
        }

        // technician 可以查看分配给自己的
        if (hasRole(principal, ROLE_TECHNICIAN)
                && currentUserId.equals(entity.getAssigneeId())) {
            return;
        }

        throw new BusinessException(ResultCode.TICKET_NOT_FOUND, "工单不存在或无权访问");
    }

    private boolean hasRole(LoginUserPrincipal principal, String role) {
        return principal.getRoles() != null && principal.getRoles().contains(role);
    }

    // ---- 转换 ----

    private TicketListItemVO toListItemVO(TicketEntity e) {
        TicketListItemVO vo = new TicketListItemVO();
        vo.setId(e.getId());
        vo.setTicketNo(e.getTicketNo());
        vo.setTitle(e.getTitle());
        vo.setCategoryId(e.getCategoryId());
        vo.setPriority(e.getPriority());
        vo.setStatus(e.getStatus());
        vo.setSubmitterId(e.getSubmitterId());
        vo.setAssigneeId(e.getAssigneeId());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }

    private TicketDetailVO toDetailVO(TicketEntity e) {
        TicketDetailVO vo = new TicketDetailVO();
        vo.setId(e.getId());
        vo.setTicketNo(e.getTicketNo());
        vo.setTitle(e.getTitle());
        vo.setDescription(e.getDescription());
        vo.setCategoryId(e.getCategoryId());
        vo.setPriority(e.getPriority());
        vo.setStatus(e.getStatus());
        vo.setSubmitterId(e.getSubmitterId());
        vo.setAssigneeId(e.getAssigneeId());
        vo.setAssignedAt(e.getAssignedAt());
        vo.setResolvedAt(e.getResolvedAt());
        vo.setClosedAt(e.getClosedAt());
        vo.setDueAt(e.getDueAt());
        vo.setSource(e.getSource());
        vo.setAiSummary(e.getAiSummary());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }

    // ---- 工单编号 ----

    private String buildTicketNo(Long id) {
        String year = String.valueOf(LocalDateTime.now().getYear());
        return "INC-" + year + "-" + String.format("%04d", id);
    }
}