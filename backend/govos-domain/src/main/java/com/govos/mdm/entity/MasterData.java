package com.govos.mdm.entity;

import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Master reference data entry (lookup values, configuration codes, enumerations).
 */
@Entity
@Table(name = "mdm_master_data", schema = "govos")
public class MasterData extends AuditableEntity {

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Column(name = "data_key", nullable = false, length = 200)
    private String key;

    @Column(name = "data_value", nullable = false, length = 500)
    private String value;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "system_defined", nullable = false)
    private Boolean systemDefined = false;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getSystemDefined() {
        return systemDefined;
    }

    public void setSystemDefined(Boolean systemDefined) {
        this.systemDefined = systemDefined;
    }
}
