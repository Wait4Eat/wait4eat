package com.example.wait4eat.domain.waiting.controller;

import com.example.wait4eat.domain.waiting.dto.response.MyWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.service.WaitingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @GetMapping("/api/v1/stores/{storeId}/waitings")
    public ResponseEntity<Page<WaitingResponse>> getWaitings(
            @PathVariable long storeId,
            @RequestParam(required = false) WaitingStatus status,
            @PageableDefault(page = 1, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(waitingService.getWaitings(storeId, status, pageable));
    }

    @GetMapping("/api/v1/waitings/me")
    public ResponseEntity<MyWaitingResponse> getMyWaiting(
            //@AuthenticationPrincipal AuthUser authUser
    ) {
        //return ResponseEntity.ok(waitingService.getMyWaiting(authUser.getId()));
        return null;
    }

}


