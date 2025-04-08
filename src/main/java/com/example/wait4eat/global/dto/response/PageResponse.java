package com.example.wait4eat.global.dto.response;

import com.example.wait4eat.global.dto.consts.ApiMessage;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class PageResponse<T> extends ApiResponse {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean isLast;

    private PageResponse(Page<T> pageData, String message) {
        super(HttpStatus.OK, true, message);
        this.content = pageData.getContent();
        this.page = pageData.getNumber();
        this.size = pageData.getSize();
        this.totalElements = pageData.getTotalElements();
        this.totalPages = pageData.getTotalPages();
        this.isLast = pageData.isLast();
    }

    public static <T> PageResponse<T> from(Page<T> pageData) {
        return new PageResponse<>(pageData, ApiMessage.DEFAULT_SUCCESS_MESSAGE);
    }

    public static <T> PageResponse<T> of(Page<T> pageData, String message) {
        return new PageResponse<>(pageData, message);
    }
}
