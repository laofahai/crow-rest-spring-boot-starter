package org.teamswift.crow.rest.provider.jpa;

import com.sun.istack.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@NoRepositoryBean
public interface ICrowRepositoryJpa<I extends Serializable, T extends ICrowEntity<I, ?>>
        extends JpaRepository<T, I>, JpaSpecificationExecutor<T> {

    List<T> findAllByIdIn(@NotNull Collection<Integer> ids);

}
