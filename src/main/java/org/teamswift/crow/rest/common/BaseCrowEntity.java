package org.teamswift.crow.rest.common;

import jdk.jfr.Label;
import lombok.Data;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.Date;

@Data
@MappedSuperclass
public class BaseCrowEntity {

    @Label("Deleted At")
    private Date deletedDate;

    @Transient
    @Label("Is Deleted")
    private boolean deleted;

    public void setDeleted(boolean deleted) {
        this.deletedDate = deleted ? new Date() : null;
    }

    public boolean isDeleted() {
        return deletedDate != null;
    }

}
