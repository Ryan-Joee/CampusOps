package com.campusops.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建工单请求 DTO。
 */
@Data
public class TicketCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度为1-200")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    private Long categoryId;

    @Pattern(regexp = "low|medium|high|urgent", message = "优先级仅允许 low/medium/high/urgent")
    private String priority;
}