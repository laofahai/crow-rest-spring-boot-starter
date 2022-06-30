package org.teamswift.crow.rest.common;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.teamswift.crow.rest.annotation.I18N;
import org.teamswift.crow.rest.annotation.SystemGeneratedValue;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.Date;

@Data
@MappedSuperclass
abstract public class BaseCrowEntity<ID, V extends ICrowIO> implements ICrowEntity<ID, V> {

    @I18N("DeletedAt")
    @SystemGeneratedValue
    private Date deletedDate;

    @Transient
    @I18N("IsDeleted")
    @SystemGeneratedValue
    private boolean deleted;

    @CreatedDate
    @I18N("CreatedAt")
    @SystemGeneratedValue
    private Date createdAt;

    @LastModifiedDate
    @I18N("ModifiedAt")
    @SystemGeneratedValue
    private Date modifiedAt;

    @Override
    public void setDeleted(boolean deleted) {
        this.deletedDate = deleted ? new Date() : null;
    }

    @Override
    public boolean isDeleted() {
        return deletedDate != null;
    }

}
