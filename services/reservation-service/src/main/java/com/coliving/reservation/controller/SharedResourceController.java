
package com.coliving.reservation.controller;

import com.coliving.reservation.entity.SharedResource;
import com.coliving.reservation.service.SharedResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation-service/resources")
@RequiredArgsConstructor
public class SharedResourceController {

    private final SharedResourceService sharedResourceService;

    // 건물별 공유 자원 목록 조회
    @GetMapping
    public List<SharedResource> findByBuildingId(
            @RequestParam Long buildingId) {

        return sharedResourceService.findByBuildingId(buildingId);
    }

    // 공유 자원 상세 조회
    @GetMapping("/{id}")
    public SharedResource findById(@PathVariable Long id) {

        return sharedResourceService.findById(id);
    }

}