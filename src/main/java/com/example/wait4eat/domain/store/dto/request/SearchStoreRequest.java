package com.example.wait4eat.domain.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class SearchStoreRequest {

    // 검색 조건
    private String name; // 가게 이름 (like 검색)
    private String address; // 주소 (like 검색)
    private String description; // 설명 (like 검색)
    private LocalTime openTime; // 오픈 시간 (필터링)
    private LocalTime closeTime; // 마감 시간 (필터링)

    // 페이징 정보
    @Min(value = 0, message = "page는 0 이상이어야 합니다.")
    private int page = 0; // 기본값: 0

    @Min(value = 1, message = "size는 1 이상이어야 합니다.")
    private int size = 10; // 기본값: 10

    // 정렬 정보
    @Pattern(regexp = "createdAt|depositAmount|waitingTeamCount",
            message = "sort는 createdAt, depositAmount, waitingTeamCount 중 하나여야 합니다.")
    private String sort = "createdAt"; // 기본 정렬 필드 (depositAmount, waitingTeamCount 등 가능)

    @Pattern(regexp = "asc|desc",
            message = "sortDirection은 asc 또는 desc이어야 합니다.")
    private String sortDirection = "desc"; // 기본 정렬 방향 (asc, desc 가능)

    @Builder
    public SearchStoreRequest(
            String name,
            String address,
            String description,
            LocalTime openTime,
            LocalTime closeTime,
            Integer page,
            Integer size,
            String sort,
            String sortDirection
    ) {
        System.out.println("Building SearchStoreRequest: name=" + name); // 디버깅 로그
        this.name = name;
        this.address = address;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 10;
        this.sort = sort != null ? sort : "createdAt";
        this.sortDirection = sortDirection != null ? sortDirection : "desc";
    }

    // 기본 생성자 (빌더 패턴과 함께 사용 가능하도록)
    public SearchStoreRequest() {
        System.out.println("Default constructor called");
        this.page = 0;
        this.size = 10;
        this.sort = "createdAt";
        this.sortDirection = "desc";
    }
}
