package com.govos.org.entity;

import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "org_designation", schema = "govos")
public class Designation extends AuditableEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "grade", length = 50)
    private String grade;

    @Column(name = "description", length = 1000)
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
