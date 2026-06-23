package com.campusops.common.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 分页响应对象。与接口设计文档约定的字段保持一致。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> items;
    private long page;
    private long pageSize;
    private long total;

    public static <T> PageResult<T> empty(long page, long pageSize) {
        return new PageResult<>(Collections.emptyList(), page, pageSize, 0L);
    }

    public static <T> PageResult<T> of(List<T> items, long page, long pageSize, long total) {
        return new PageResult<>(items, page, pageSize, total);
    }
}
