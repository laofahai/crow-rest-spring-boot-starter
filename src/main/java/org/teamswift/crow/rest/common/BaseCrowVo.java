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
abstract public class BaseCrowVo implements ICrowVo {

    private Date deletedDate;

    private boolean deleted;

    private Date createdAt;

    private Date modifiedAt;

}