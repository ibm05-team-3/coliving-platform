
package com.coliving.reservation.repository;

import com.coliving.reservation.entity.SharedResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedResourceRepository
        extends JpaRepository<SharedResource, Long> {

    List<SharedResource> findByBuildingId(Long buildingId);
}
