package org.teamswift.crow.rest.common;

import org.teamswift.crow.rest.provider.jpa.ICrowRepositoryJpa;

public interface ICrowService<ID, E extends ICrowEntity<ID, ?>, R extends ICrowRepositoryJpa<ID, E>> {

    R getRepository();

    default E findOneById(ID id) {
        return getRepository().findById(id).orElse(null);
    }

}
