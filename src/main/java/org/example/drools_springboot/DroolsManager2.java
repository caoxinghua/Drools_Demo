package org.example.drools_springboot;

import org.example.drools_springboot.entity.DroolsRule;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DroolsManager2 {

    private final KieServices kieServices = KieServices.Factory.get();
    private final Map<String, List<DroolsRule>> droolsRuleMap = new HashMap<>();

    /**
     * Add or update rule file
     */
    public void addOrUpdateRule(DroolsRule droolsRule) {
        long startTime = System.currentTimeMillis();

        String drl = generateDRL(droolsRule);

        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(drl, ResourceType.DRL);

        Results results = kieHelper.verify();
        if (results.hasMessages(Message.Level.ERROR)) {
            log.error(results.getMessages().toString());
            throw new RuntimeException("Loading rules failed");
        }

        saveDroolsRule(droolsRule);

        long endTime = System.currentTimeMillis();
        System.out.println("addOrUpdateRule execution time: " + (endTime - startTime) + " ms");
    }

    private String generateDRL(DroolsRule droolsRule) {
        StringBuilder drlBuilder = new StringBuilder();
        drlBuilder.append("package ").append(droolsRule.getKiePackageName()).append(";\n");
        drlBuilder.append("rule \"").append(droolsRule.getRuleName()).append("\"\n");
        drlBuilder.append("when\n");
        drlBuilder.append("    ").append(droolsRule.getIfCondition()).append("\n");
        drlBuilder.append("then\n");
        drlBuilder.append("    ").append(droolsRule.getThenCondition()).append("\n");
        drlBuilder.append("end\n");
        return drlBuilder.toString();
    }

    /**
     * Fire Rule
     */
    public String fireRule(String containerName, String kieBaseName, Integer param) {
        long startTime = System.currentTimeMillis();

        // Retrieve the list of DroolsRule objects based on containerName and kieBaseName
        List<DroolsRule> droolsRuleList = getDroolsRuleList(containerName, kieBaseName);
        if (droolsRuleList.isEmpty()) {
            throw new RuntimeException("No DroolsRule found for containerName: " + containerName + ", kieBaseName: " + kieBaseName);
        }

        KieHelper kieHelper = new KieHelper();
        for (DroolsRule droolsRule : droolsRuleList) {
            String drl = generateDRL(droolsRule);
            kieHelper.addContent(drl, ResourceType.DRL);
        }

        KieBase kieBase = kieHelper.build();
        KieSession kieSession = kieBase.newKieSession();

        kieSession.insert(param);
        kieSession.fireAllRules();
        kieSession.dispose();

        long endTime = System.currentTimeMillis();
        System.out.println("fireRule execution time: " + (endTime - startTime) + " ms");
        return "OK";
    }

    private List<DroolsRule> getDroolsRuleList(String containerName, String kieBaseName) {
        String key = generateKey(containerName, kieBaseName);
        return droolsRuleMap.getOrDefault(key, new ArrayList<>());
    }

    private void saveDroolsRule(DroolsRule droolsRule) {
        String key = generateKey(droolsRule.getContainerName(), droolsRule.getKieBaseName());
        droolsRuleMap.computeIfAbsent(key, k -> new ArrayList<>()).add(droolsRule);
    }

    private String generateKey(String containerName, String kieBaseName) {
        return containerName + "_" + kieBaseName;
    }
}