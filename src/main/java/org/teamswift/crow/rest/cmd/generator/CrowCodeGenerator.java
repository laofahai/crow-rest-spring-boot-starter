package org.teamswift.crow.rest.cmd.generator;

import com.google.common.base.Strings;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.lang.Nullable;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.teamswift.crow.rest.exception.BusinessException;
import org.teamswift.crow.rest.utils.CrowBeanUtils;
import org.teamswift.crow.rest.utils.Scaffolds;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ShellComponent
public class CrowCodeGenerator {

    private final Configuration cfg;

    private final List<String> availableFiles = Arrays.asList(
            "controller", "vo", "dto", "entity", "repository"
    );

    public CrowCodeGenerator() throws IOException {

        String path = "/templates/codeTemplate";

        cfg = new Configuration(Configuration.getVersion());
        cfg.setClassForTemplateLoading(getClass(), path);
        cfg.setTemplateLoader(
                new ClassTemplateLoader(getClass(), path)
        );
        cfg.setDefaultEncoding("utf-8");
    }

    @ShellMethod(
            group = "Crow",
            value = "Code generator for crow rest"
    )
    public void crowGenerate(
            @NotNull
            @Pattern(
                    regexp = "[a-zA-Z]+\\.[a-zA-Z]+"
            )
            String moduleName,
            @ShellOption(
                    value = {
                            "--pk", "-k"
                    },
                    defaultValue = "Integer"
            )
            String primaryKeyType,
            @ShellOption(
                    value = {
                            "--superEntity",
                            "-s"
                    },
                    defaultValue = "org.teamswift.crow.rbac.common.CrowOrgLimitedEntity"
            )
            String superEntityClass,
            @ShellOption(
                    value = {
                        "--package", "-p"
                    },
                    defaultValue = ""
            )
            @Nullable
            String packageName,
            @ShellOption(
                    value = {"--files", "-f"},
                    defaultValue = "controller,vo,dto,entity,repository"
            )
            List<String> files
    ) {
        String[] moduleSplit = moduleName.split("\\.");
        String app = Scaffolds.ucfirst(moduleSplit[0]);
        String module = Scaffolds.ucfirst(moduleSplit[1]);

        if(Strings.isNullOrEmpty(packageName)) {
            Map<String, Object> annotatedBeans = CrowBeanUtils.getApplicationContext().getBeansWithAnnotation(SpringBootApplication.class);
            String mainClass = annotatedBeans.isEmpty() ? null : annotatedBeans.values().toArray()[0].getClass().getName();

            if(mainClass == null) {
                throw new BusinessException("Can't get application main class");
            }
            String[] split = mainClass.split("\\.");
            packageName = String.join(".", Arrays.copyOfRange(split, 0, split.length - 1));
        }

        String[] split = superEntityClass.split("\\.");
        String superClass = split[split.length - 1];


        CrowCodeGeneratorModel model = new CrowCodeGeneratorModel();
        model.setPackageName(packageName);
        model.setApp(app);
        model.setModule(module);
        model.setAppLC(Scaffolds.lcfirst(app));
        model.setModuleLC(Scaffolds.lcfirst(module));
        model.setSuperEntity(superClass);
        model.setSuperEntityClass(superEntityClass);
        model.setPrimaryKeyType(primaryKeyType);

        files.forEach(file -> {
            if(!availableFiles.contains(file)) {
                System.out.println(file + " is not an available template");
                return;
            }

            generate(file, model);
        });

    }

    public void generate(String templateName, CrowCodeGeneratorModel model) {
        String error = "An error occurred when creating: " + templateName;

        Template template;
        try {
            template = cfg.getTemplate(templateName + ".ftl");
        } catch (IOException e) {
            System.out.print(error);
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        String dir = String.format(
                "%s/src/main/java/%s/%s/%s",
                System.getProperty("user.dir"),
                model.getPackageName().replaceAll("\\.", "/"),
                model.getAppLC(),
                templateName
        );

        File d = new File(dir);
        d.mkdirs();

        String filePath = dir + "/" + getFileName(model.getModule(), templateName) + ".java";

        File f = new File(filePath);
        if(f.exists()) {
            throw new BusinessException("Target file already exists: " + templateName);
        }

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        OutputStreamWriter out = new OutputStreamWriter(fos);

        try {
            template.process(model, out);
            out.close();
            fos.close();
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Success generate file: " + templateName);
    }

    private String getFileName(String module, String template) {
        if("entity".equals(template)) {
            return module;
        }
        return module + Scaffolds.ucfirst(template);
    }
    
}
