package org.example.drools_springboot;

import org.example.drools_springboot.entity.DroolsRule;
import lombok.extern.slf4j.Slf4j;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
@Slf4j
public class DroolsManager {

    private final KieServices kieServices = KieServices.get();
    private final Map<String, KieContainer> kieContainerMap = new HashMap<>();
    private final Map<String, KieFileSystem> kieFileSystemMap = new HashMap<>();

    /**
     * Add or update rule file
     */
    public void addOrUpdateRule(DroolsRule droolsRule) {
        long startTime = System.currentTimeMillis();
        String containerName = droolsRule.getContainerName();
        String kieBaseName = droolsRule.getKieBaseName();
        KieContainer kieContainer = null;
        KieFileSystem kieFileSystem = kieFileSystemMap.computeIfAbsent(containerName, k -> kieServices.newKieFileSystem());

        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();
        KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel(kieBaseName);
        kieBaseModel.setDefault(false);

        kieContainer = kieContainerMap.get(containerName);
        if (null != kieContainer) {
            Collection<String> kieBaseNames = kieContainer.getKieBaseNames();
            if (kieBaseNames.contains(kieBaseName)) {
                KieBase kieBase = kieContainer.getKieBase(kieBaseName);
                if (kieBase != null) {
                    Collection<KiePackage> kiePackages = kieBase.getKiePackages();
                    for (KiePackage kiePackage : kiePackages) {
                        kieBaseModel.addPackage(kiePackage.getName());
                    }
                }
            }
        }

        kieBaseModel.newKieSessionModel(kieBaseName + "-session").setDefault(false);
        kieBaseModel.addPackage(droolsRule.getKiePackageName());

        String file = "src/main/resources/" + droolsRule.getKiePackageName() + "/" + droolsRule.getRuleId() + ".drl";
        System.out.println("Load rules: " + file);
        kieFileSystem.write(file, droolsRule.getRuleContent());

        String kmoduleXml = kieModuleModel.toXML();
        log.info("Load kmodule.xml:[\n{}]", kmoduleXml);
        kieFileSystem.writeKModuleXML(kmoduleXml);

        //Update Container
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        Results results = kieBuilder.getResults();
        List<Message> messages = results.getMessages(Message.Level.ERROR);
        if (null != messages && !messages.isEmpty()) {
            log.error(messages.toString());
            throw new RuntimeException("Loading rules failed");
        }

        if (null == kieContainer) {
            kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        } else {
            ((KieContainerImpl) kieContainer).updateToKieModule((InternalKieModule) kieBuilder.getKieModule());
        }

        kieContainerMap.put(containerName, kieContainer);
        long endTime = System.currentTimeMillis();
        System.out.println("addOrUpdateRule execution time: " + (endTime - startTime) + " ms");
    }

    /**
     * Fire Rule
     */
    public String fireRule(String containerName, String kieBaseName, Integer param) {
        long startTime = System.currentTimeMillis();
        KieContainer kieContainer = kieContainerMap.get(containerName);
        if (kieContainer == null) {
            throw new RuntimeException("KieContainer not found for container name: " + containerName);
        }


        KieBase kieBase = kieContainer.getKieBase(kieBaseName);
        if (kieBase == null) {
            throw new RuntimeException("KieBase not found for KieBase name: " + kieBaseName);
        }

        for (KiePackage kiePackage : kieBase.getKiePackages()) {
            for (org.kie.api.definition.rule.Rule rule : kiePackage.getRules()) {
                System.out.println(rule.getPackageName() + "." + rule.getName() + "\n");
            }
        }

        KieSession kieSession = kieContainer.newKieSession(kieBaseName + "-session");
        kieSession.insert(param);
        kieSession.fireAllRules();
        kieSession.dispose();
        long endTime = System.currentTimeMillis();
        System.out.println("fireRule execution time: " + (endTime - startTime) + " ms");
        return "OK";
    }
}