package com.coliving.reservation.service;


import com.coliving.reservation.entity.SharedResource;
import com.coliving.reservation.repository.SharedResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
@RequiredArgsConstructor
public class SharedResourceService {

    private final SharedResourceRepository sharedResourceRepository;

    // 건물별 공유 자원 목록 조회
    public List<SharedResource> findByBuildingId(Long buildingId) {

        return sharedResourceRepository.findByBuildingId(buildingId);
    }

    // 공유 자원 등록
    public SharedResource createResource(SharedResource sharedResource) {

        return sharedResourceRepository.save(sharedResource);
    }

    // 공유 자원 상세 조회
    public SharedResource findById(Long id) {

        return sharedResourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공유 자원을 찾을 수 없습니다."));

    }
    // 공용 자원 수정
    public SharedResource updateResource(Long id, SharedResource updatedResource) {

        SharedResource sharedResource = sharedResourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공유 자원을 찾을 수 없습니다."));

        if (updatedResource.getResourceName() != null) {
            sharedResource.setResourceName(updatedResource.getResourceName());
        }

        if (updatedResource.getResourceType() != null) {
            sharedResource.setResourceType(updatedResource.getResourceType());
        }

        if (updatedResource.getDescription() != null) {
            sharedResource.setDescription(updatedResource.getDescription());
        }

        if (updatedResource.getResourceStatus() != null) {
            sharedResource.setResourceStatus(updatedResource.getResourceStatus());
        }

        if (updatedResource.getMaxUsageHours() != null) {
            sharedResource.setMaxUsageHours(updatedResource.getMaxUsageHours());
        }

        if (updatedResource.getBuildingId() != null) {
            sharedResource.setBuildingId(updatedResource.getBuildingId());
        }

        if (updatedResource.getImageUrl() != null) {
            sharedResource.setImageUrl(updatedResource.getImageUrl());
        }

        if (updatedResource.getLocation() != null) {
            sharedResource.setLocation(updatedResource.getLocation());
        }

        if (updatedResource.getCapacity() != null) {
            sharedResource.setCapacity(updatedResource.getCapacity());
        }

        if (updatedResource.getOpeningTime() != null) {
            sharedResource.setOpeningTime(updatedResource.getOpeningTime());
        }

        if (updatedResource.getClosingTime() != null) {
            sharedResource.setClosingTime(updatedResource.getClosingTime());
        }

        return sharedResourceRepository.save(sharedResource);
    }

}
