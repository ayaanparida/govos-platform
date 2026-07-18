package com.govos.org.repository;

import com.govos.org.entity.EmployeeNumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeNumberSequenceRepository extends JpaRepository<EmployeeNumberSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM EmployeeNumberSequence s WHERE s.year = :year")
    Optional<EmployeeNumberSequence> findByYearForUpdate(@Param("year") int year);
}
