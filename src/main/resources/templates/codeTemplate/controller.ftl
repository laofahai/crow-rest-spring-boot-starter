package ${packageName}.${appLC}.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.teamswift.crow.rest.provider.jpa.CrowControllerJpa;

import ${packageName}.${appLC}.entity.${module};
import ${packageName}.${appLC}.vo.${module}Vo;
import ${packageName}.${appLC}.dto.${module}Dto;

@RequestMapping("/${appLC}/${moduleLC}")
@RestController
public class ${module}Controller extends CrowControllerJpa<
    ${primaryKeyType},
    ${module},
    ${module}Vo,
    ${module}Dto
> {


}