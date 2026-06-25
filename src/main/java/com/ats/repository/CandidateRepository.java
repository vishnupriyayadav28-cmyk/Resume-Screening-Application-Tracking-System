package com.ats.repository;

import com.ats.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository interface for Candidate entity.
 * Provides all CRUD operations automatically.
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
}
