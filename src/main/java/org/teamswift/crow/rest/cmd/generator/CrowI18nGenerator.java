package org.teamswift.crow.rest.cmd.generator;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.teamswift.crow.rest.handler.dataStructure.EntityMeta;
import org.teamswift.crow.rest.service.CrowDataStructureService;
import org.teamswift.crow.rest.utils.Scaffolds;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@ShellComponent
public class CrowI18nGenerator {

    private final CrowDataStructureService dataStructureService;

    public CrowI18nGenerator(CrowDataStructureService dataStructureService) {
        this.dataStructureService = dataStructureService;
    }

    @ShellMethod(
            group = "Crow"
    )
    public void crowGenerateI18n(
            @ShellOption(
                    defaultValue = "messages"
            )
            String fileName
    ) {

        String path = String.format(
                "%s/src/main/resources/i18n/%s.properties",
                System.getProperty("user.dir"),
                fileName
        );

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(path, true);
            fos.write("\n\n".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Map<String, EntityMeta> raw = dataStructureService.getEntitiesDataStructureMap();
        for(String name: raw.keySet()) {
            EntityMeta entityMeta = raw.get(name);
            if(entityMeta.isBelongsToCrow()) {
                continue;
            }
            entityMeta.getFieldsMap().forEach((fieldName, fieldStructure) -> {

                if(fieldStructure.isJsonIgnore()) {
                    return;
                }

                String lang = String.format(
                        "%s=%s\n",
                        fieldStructure.getLabel(),
                        Scaffolds.camelToSpace(fieldName)
                );
                try {
                    fos.write(lang.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            try {
                fos.write("\n".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
