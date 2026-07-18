package com.govos.org.service;

import com.govos.org.entity.EmployeeNumberSequence;
import com.govos.org.repository.EmployeeNumberSequenceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/**
 * Generates immutable business employee numbers in the format {@code EMP-YYYY-NNNNNN}.
 * UUID remains the primary key on {@link com.govos.org.entity.Employee}.
 */
@Component
public class EmployeeNumberGenerator {

    private static final String FORMAT = "EMP-%d-%06d";

    private final EmployeeNumberSequenceRepository sequenceRepository;

    public EmployeeNumberGenerator(EmployeeNumberSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Transactional
    public String generateNext() {
        int year = Year.now().getValue();
        EmployeeNumberSequence sequence = sequenceRepository.findByYearForUpdate(year)
                .orElseGet(() -> {
                    EmployeeNumberSequence created = new EmployeeNumberSequence();
                    created.setYear(year);
                    created.setLastSequence(0L);
                    return created;
                });

        long next = sequence.getLastSequence() + 1;
        sequence.setLastSequence(next);
        sequenceRepository.save(sequence);

        return FORMAT.formatted(year, next);
    }
}
