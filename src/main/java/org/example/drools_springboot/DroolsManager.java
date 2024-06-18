package org.example.drools_springboot;

import org.example.drools_springboot.entity.DroolsRule;
import lombok.extern.slf4j.Slf4j;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
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

    private final KieServices kieServices = KieServices.Factory.get();
    private final Map<String, KieContainer> kieContainerMap = new HashMap<>();
    private final Map<String, KieFileSystem> kieFileSystemMap = new HashMap<>();

    /**
     * Add or update rule file
     */
    public void addOrUpdateRule(DroolsRule droolsRule) {
        long startTime = System.currentTimeMillis();
        String containerName = droolsRule.getContainerName();
        String kieBaseName = droolsRule.getKieBaseName();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();
        kieModuleModel.newKieBaseModel(kieBaseName)
                .setDefault(false)
                .addPackage(droolsRule.getKiePackageName())
                .newKieSessionModel(kieBaseName + "-session")
                .setDefault(false);

        String file = "src/main/resources/" + droolsRule.getKiePackageName() + "/" + droolsRule.getRuleId() + ".drl";
        kieFileSystem.write(file, droolsRule.getRuleContent());

        String kmoduleXml = kieModuleModel.toXML();
        kieFileSystem.writeKModuleXML(kmoduleXml);

        // Build KieContainer
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        Results results = kieBuilder.getResults();
        List<Message> messages = results.getMessages(Message.Level.ERROR);
        if (null != messages && !messages.isEmpty()) {
            log.error(messages.toString());
            throw new RuntimeException("Loading rules failed");
        }

        KieContainer kieContainer = kieContainerMap.get(containerName);
        if (kieContainer == null) {
            kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        } else {
            kieContainer.updateToVersion(kieServices.getRepository().getDefaultReleaseId());
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

        KieSession kieSession = kieBase.newKieSession();

        kieSession.insert(param);
        kieSession.fireAllRules();
        kieSession.dispose();

        long endTime = System.currentTimeMillis();
        System.out.println("fireRule execution time: " + (endTime - startTime) + " ms");
        return "OK";
    }
}