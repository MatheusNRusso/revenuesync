package com.mtnrs.revenuesync.repository;

import com.mtnrs.revenuesync.domain.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, Long> {
}
