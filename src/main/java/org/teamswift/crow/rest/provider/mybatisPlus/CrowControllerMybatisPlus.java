package org.teamswift.crow.rest.provider.mybatisPlus;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.beans.BeanUtils;
import org.teamswift.crow.rest.common.CrowController;
import org.teamswift.crow.rest.common.ICrowDBService;
import org.teamswift.crow.rest.common.ICrowEntity;
import org.teamswift.crow.rest.common.ICrowIO;
import org.teamswift.crow.rest.utils.CrowBeanUtils;
import org.teamswift.crow.rest.utils.GenericUtils;

import java.io.Serializable;

public class CrowControllerMybatisPlus <
        ID extends Serializable,
        T extends ICrowEntity<ID, V>,
        V extends ICrowIO,
        D extends ICrowIO,
        M extends BaseMapper<T>>
        implements CrowController<ID, T, V, D> {

    protected M getMapper(){
        Class<M> mapperClass = (Class<M>) GenericUtils.get(this.getClass(), 3);
        return CrowBeanUtils.getBean(mapperClass);
    }

    public ICrowDBService<ID, D, T> getCrowProvider() {
        return new CrowDBServiceMybatisPlus<>(getEntityCls(), getMapper());
    }

}