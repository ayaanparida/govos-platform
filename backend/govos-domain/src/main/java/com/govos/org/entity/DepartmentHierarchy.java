package com.govos.org.entity;

import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "org_department_hierarchy", schema = "govos")
public class DepartmentHierarchy extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_department_id", nullable = false)
    private Department parentDepartment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_department_id", nullable = false)
    private Department childDepartment;

    public Department getParentDepartment() {
        return parentDepartment;
    }

    public void setParentDepartment(Department parentDepartment) {
        this.parentDepartment = parentDepartment;
    }

    public Department getChildDepartment() {
        return childDepartment;
    }

    public void setChildDepartment(Department childDepartment) {
        this.childDepartment = childDepartment;
    }
}
