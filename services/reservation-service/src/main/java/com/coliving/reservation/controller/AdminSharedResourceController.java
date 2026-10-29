package com.coliving.reservation.controller;


import com.coliving.reservation.entity.SharedResource;
import com.coliving.reservation.service.SharedResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservation-service/admin/resources")
@RequiredArgsConstructor
public class AdminSharedResourceController {

    private final SharedResourceService sharedResourceService;

    // 관리자 공용 자원 등록
    @PostMapping
    public SharedResource createResource(
            @RequestBody SharedResource sharedResource) {

        return sharedResourceService.createResource(sharedResource);
    }

    // 관리자 공용 자원 수정
    @PatchMapping("/{id}")
    public SharedResource updateResource(
            @PathVariable Long id,
            @RequestBody SharedResource updatedResource) {

        return sharedResourceService.updateResource(id, updatedResource);
    }
}
