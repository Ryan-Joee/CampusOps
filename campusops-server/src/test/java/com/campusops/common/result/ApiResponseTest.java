package com.campusops.common.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void successWithoutData() {
        ApiResponse<Void> response = ApiResponse.success();

        assertThat(response.getCode()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.getMessage()).isEqualTo(ResultCode.SUCCESS.getMessage());
        assertThat(response.getData()).isNull();
        assertThat(response.getTraceId()).isNull();
    }

    @Test
    void successWithData() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertThat(response.getCode()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.getData()).isEqualTo("hello");
    }

    @Test
    void errorFromResultCode() {
        ApiResponse<Void> response = ApiResponse.error(ResultCode.BUSINESS_ERROR);

        assertThat(response.getCode()).isEqualTo(ResultCode.BUSINESS_ERROR.getCode());
        assertThat(response.getMessage()).isEqualTo(ResultCode.BUSINESS_ERROR.getMessage());
        assertThat(response.getData()).isNull();
    }

    @Test
    void errorWithCustomMessage() {
        ApiResponse<Void> response = ApiResponse.error("TICKET_NOT_FOUND", "工单不存在或无权限访问");

        assertThat(response.getCode()).isEqualTo("TICKET_NOT_FOUND");
        assertThat(response.getMessage()).isEqualTo("工单不存在或无权限访问");
    }
}
