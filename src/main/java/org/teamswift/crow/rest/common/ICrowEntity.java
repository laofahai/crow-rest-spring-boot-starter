package org.teamswift.crow.rest.common;

import java.io.Serializable;
import java.util.Date;

/**
 * The basic entity provides primary key and soft delete feature.
 * @param <ID> The primary key type
 */
public interface ICrowEntity<ID extends Serializable, V extends ICrowIO> extends ICrowIO {

    ID getId();

    void setId(ID id);

    boolean isDeleted();

    void setDeleted(boolean deleted);

    Date getDeletedDate();

    void setDeletedDate(Date date);

}
