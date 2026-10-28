package com.coliving.platform.note.repository;

import com.coliving.platform.note.entity.GuestInquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestInquiryRepository extends JpaRepository<GuestInquiryEntity, Long> {
}
