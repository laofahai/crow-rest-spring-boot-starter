package ${packageName}.${appLC}.entity;

import jdk.jfr.Label;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;

import org.teamswift.crow.rest.annotation.I18N;
import ${superEntityClass};
import ${packageName}.${app}.vo.${module}Vo;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class ${module} extends ${superEntity}<
    ${primaryKeyType},
    ${module}Vo
> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @I18N("crow.id")
    private Integer id;

    @Override
    public Integer gtId() {
        return id;
    }

    // generator placeholder, don't remove this line.

}