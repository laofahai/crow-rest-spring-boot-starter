package org.teamswift.crow.rest.common;

import org.teamswift.crow.rest.exception.impl.DataNotFoundException;
import org.teamswift.crow.rest.provider.jpa.ICrowRepositoryJpa;

import java.io.Serializable;

public interface ICrowService<ID extends Serializable, E extends ICrowEntity<ID, ?>, R extends ICrowRepositoryJpa<ID, E>> {

    R getRepository();

    default E findOneById(ID id) {
        return getRepository().findById(id).orElse(null);
    }

    default E findOneByIdOrThrow(ID id) {
        return getRepository().findById(id).orElseThrow(() -> {
            throw new DataNotFoundException();
        });
    }

}
