package ${packageName}.${appLC}.repository;

import org.springframework.stereotype.Repository;
import org.teamswift.crow.rest.provider.jpa.ICrowRepositoryJpa;

import ${packageName}.${appLC}.entity.${module};

@Repository
public interface ${module}Repository extends ICrowRepositoryJpa<${primaryKeyType}, ${module}> {

}

