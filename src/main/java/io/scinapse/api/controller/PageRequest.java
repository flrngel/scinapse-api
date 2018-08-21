package io.scinapse.api.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

@Getter
@RequiredArgsConstructor
public class PageRequest {
    private final int page;
    private final int size;
    private final String sort;

    public int getOffset() {
        return page * size;
    }

    public Pageable toPageable() {
        return new org.springframework.data.domain.PageRequest(this.page, this.size);
    }

    public static Pageable defaultPageable() {
        return defaultPageable(10);
    }

    public static Pageable defaultPageable(int size) {
        return new org.springframework.data.domain.PageRequest(0, size);
    }

}
