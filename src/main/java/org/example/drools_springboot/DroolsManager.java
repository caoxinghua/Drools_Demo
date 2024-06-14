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
     * If KBase exists
     */
    public boolean existsKieBase(String containerName, String kieBaseName) {
        KieContainer kieContainer = kieContainerMap.get(containerName);
        if (null == kieContainer) {
            return false;
        }
        Collection<String> kieBaseNames = kieContainer.getKieBaseNames();
        if (kieBaseNames.contains(kieBaseName)) {
            return true;
        }
        log.info("Missing KieBase:{}", kieBaseName);
        return false;
    }
    /**
     * Add or update rule file
     */
    public void addOrUpdateRule(DroolsRule droolsRule) {
        String containerName = droolsRule.getContainerName();
        String kieBaseName = droolsRule.getKieBaseName();
        KieFileSystem kieFileSystem = kieFileSystemMap.computeIfAbsent(containerName, k -> kieServices.newKieFileSystem());

        boolean existsKieBase = existsKieBase(containerName, kieBaseName);
        KieModuleModel kieModuleModel;
        KieBaseModel kieBaseModel;
        if (!existsKieBase) {
            kieModuleModel = kieServices.newKieModuleModel();
            kieBaseModel = kieModuleModel.newKieBaseModel(kieBaseName);
            kieBaseModel.setDefault(false);
            kieBaseModel.addPackage(droolsRule.getKiePackageName());
            kieBaseModel.newKieSessionModel(kieBaseName + "-session")
                    .setDefault(false);
        } else {
            KieContainer kieContainer = kieContainerMap.get(containerName);
            kieModuleModel = kieServices.newKieModuleModel();
            kieBaseModel = kieModuleModel.newKieBaseModel(kieBaseName);
            kieBaseModel.setDefault(false);

            KieBase kieBase = kieContainer.getKieBase(kieBaseName);
            if (kieBase != null) {
                Collection<KiePackage> kiePackages = kieBase.getKiePackages();
                for (KiePackage kiePackage : kiePackages) {
                    kieBaseModel.addPackage(kiePackage.getName());
                }
            }

            // Add the session to the KieBaseModel
            kieBaseModel.newKieSessionModel(kieBaseName + "-session")
                    .setDefault(false);
        }

        kieBaseModel.addPackage(droolsRule.getKiePackageName());

        String file = "src/main/resources/" + droolsRule.getKiePackageName() + "/" + droolsRule.getRuleId() + ".drl";
        log.info("Load rules: {}", file);
        kieFileSystem.write(file, droolsRule.getRuleContent());

        String kmoduleXml = kieModuleModel.toXML();
        log.info("Load kmodule.xml:[\n{}]", kmoduleXml);
        kieFileSystem.writeKModuleXML(kmoduleXml);

        buildKieContainer(containerName, kieFileSystem);
    }

    /**
     * Create KieContainer
     */
    private void buildKieContainer(String containerName, KieFileSystem kieFileSystem) {
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        Results results = kieBuilder.getResults();
        List<Message> messages = results.getMessages(Message.Level.ERROR);
        if (null != messages && !messages.isEmpty()) {
            for (Message message : messages) {
                log.error(message.getText());
            }
            throw new RuntimeException("Loading rules failed");
        }

        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        kieContainerMap.put(containerName, kieContainer);
    }

    /**
     * Fire Rule
     */
    public String fireRule(String containerName, String kieBaseName, Integer param) {
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
        return "OK";
    }
}