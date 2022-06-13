package org.teamswift.crow.rbac.listeners;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.teamswift.crow.rbac.cmd.CrowRbacInstallation;

@Component
public class CrowRbacReady implements ApplicationListener<ApplicationReadyEvent> {

    private final CrowRbacInstallation crowRbacInstallation;

    public CrowRbacReady(CrowRbacInstallation crowRbacInstallation) {
        this.crowRbacInstallation = crowRbacInstallation;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        crowRbacInstallation.tryInstall();
    }
}
