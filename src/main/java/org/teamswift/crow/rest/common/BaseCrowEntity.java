package org.teamswift.crow.rest.common;

import lombok.Data;
import org.teamswift.crow.rest.annotation.I18N;
import org.teamswift.crow.rest.annotation.SystemGeneratedValue;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.Date;

@Data
@MappedSuperclass
public class BaseCrowEntity {

    @I18N("DeletedAt")
    @SystemGeneratedValue
    private Date deletedDate;

    @Transient
    @I18N("IsDeleted")
    private boolean deleted;

    public void setDeleted(boolean deleted) {
        this.deletedDate = deleted ? new Date() : null;
    }

    public boolean isDeleted() {
        return deletedDate != null;
    }

}
