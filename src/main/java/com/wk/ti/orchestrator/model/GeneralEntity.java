package com.wk.ti.orchestrator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@MappedSuperclass
public abstract class GeneralEntity extends BaseEntity implements Serializable {
    @JsonIgnore
    @Column(name = "modified_date", columnDefinition = "TIMESTAMP")
    @LastModifiedDate
    protected Instant modifiedDate;

    @JsonIgnore
    @Column(name = "modified_by")
    @LastModifiedBy
    protected String modifiedBy;

    @Override
    public void prePersist() {
        super.prePersist();
        if (modifiedDate == null) {
            modifiedDate = Instant.now();
        }
    }
}
