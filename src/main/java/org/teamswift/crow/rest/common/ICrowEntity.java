package org.teamswift.crow.rest.common;

import java.util.Date;

/**
 * The basic entity provides primary key and soft delete feature.
 * @param <ID> The primary key type
 */
public interface ICrowEntity<ID, V extends ICrowIO> extends ICrowIO {

    ID gtId();

    boolean isDeleted();

    void setDeleted(boolean deleted);

    Date getDeletedDate();

    void setDeletedDate(Date date);

}
