package org.example.drools_springboot;

import org.example.drools_springboot.entity.DroolsRule;
import lombok.extern.slf4j.Slf4j;
import org.example.drools_springboot.entity.Order;
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

        KieFileSystem kieFileSystem = kieFileSystemMap.computeIfAbsent(containerName, k -> kieServices.newKieFileSystem());
        KieModuleModel kieModuleModel = kieServices.newKieModuleModel();

        KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel(kieBaseName)
                .setDefault(false); //false for KBase

        kieBaseModel.newKieSessionModel(kieBaseName + "-session")
                .setDefault(false); //false for KSession

        KieContainer kieContainer = kieContainerMap.get(containerName);
        if (kieContainer != null) {
            KieBase kieBase = kieContainer.getKieBase(kieBaseName);
            if (kieBase != null) {
                Collection<KiePackage> kiePackages = kieBase.getKiePackages();
                for (KiePackage kiePackage : kiePackages) {
                    kieBaseModel.addPackage(kiePackage.getName());
                }
            }
        }

        kieBaseModel.addPackage(droolsRule.getKiePackageName());

        String file = "src/main/resources/" + droolsRule.getKiePackageName() + "/" + droolsRule.getRuleId() + ".drl";
        kieFileSystem.write(file, droolsRule.getRuleContent());

        String kmoduleXml = kieModuleModel.toXML();
        kieFileSystem.writeKModuleXML(kmoduleXml);

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();

        Results results = kieBuilder.getResults();
        List<Message> messages = results.getMessages(Message.Level.ERROR);
        if (messages != null && !messages.isEmpty()) {
            log.error(messages.toString());
            throw new RuntimeException("Loading rules failed");
        }

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
    public String fireRule(String containerName, String kieBaseName, Integer param, Map<String, Object> payload) {
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

        if (param != null) {
            kieSession.insert(param);
        } else if (payload != null) {
            for (Map.Entry<String, Object> entry : payload.entrySet()) {
                String factType = entry.getKey();
                Map<String, Object> factData = (Map<String, Object>) entry.getValue();
                Object fact = createFactObject(factType, factData);
                kieSession.insert(fact);
            }
        }

        int firedRules = kieSession.fireAllRules();
        kieSession.dispose();

        long endTime = System.currentTimeMillis();
        System.out.println("fireRule execution time: " + (endTime - startTime) + " ms");
        return "Fired " + firedRules + " rules";
    }

    private Object createFactObject(String factType, Map<String, Object> factData) {
        switch (factType) {
            case "Order":
                Order order = new Order();
                Object totalAmountObj = factData.get("totalAmount");
                if (totalAmountObj != null) {
                    if (totalAmountObj instanceof Number) {
                        order.setTotalAmount(((Number) totalAmountObj).doubleValue());
                    } else if (totalAmountObj instanceof String) {
                        try {
                            order.setTotalAmount(Double.parseDouble((String) totalAmountObj));
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Invalid totalAmount format: " + totalAmountObj);
                        }
                    } else {
                        throw new RuntimeException("Unexpected type for totalAmount: " + totalAmountObj.getClass());
                    }
                }
                return order;
            // Add cases for other fact types as needed
            default:
                throw new RuntimeException("Unknown fact type: " + factType);
        }
    }
}