package com.campusops.common.page;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {

    @Test
    void emptyPage() {
        PageResult<String> result = PageResult.empty(1, 20);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getTotal()).isZero();
    }

    @Test
    void pageWithItems() {
        PageResult<String> result = PageResult.of(List.of("a", "b"), 2, 10, 25);

        assertThat(result.getItems()).containsExactly("a", "b");
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotal()).isEqualTo(25);
    }
}
