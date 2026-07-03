package com.govos.org.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Technical sequence table for generating business employee numbers ({@code EMP-YYYY-NNNNNN}).
 */
@Entity
@Table(name = "org_employee_number_sequence", schema = "govos")
public class EmployeeNumberSequence {

    @Id
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "last_sequence", nullable = false)
    private Long lastSequence = 0L;

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getLastSequence() {
        return lastSequence;
    }

    public void setLastSequence(Long lastSequence) {
        this.lastSequence = lastSequence;
    }
}
