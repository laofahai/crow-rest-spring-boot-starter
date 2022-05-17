package org.teamswift.crow.rest.provider.jpa;

import org.teamswift.crow.rest.common.ICrowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ICrowRepositoryJpa<I, T extends ICrowEntity<I, ?>>
        extends JpaRepository<T, I>, JpaSpecificationExecutor<T> {
}
