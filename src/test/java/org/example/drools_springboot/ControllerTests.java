package org.example.drools_springboot;

import org.example.drools_springboot.entity.DroolsRule;
import org.example.drools_springboot.service.DroolsRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class ControllerTests {

    @Autowired
    private DroolsRuleService droolsRuleService;

    @Autowired
    private DroolsManager droolsManager;

    @Autowired
    private DroolsManager2 droolsManager2;

    @Test
    public void testAddAndRunRules() {
        // Add containers, packages, and rules
        for (int i = 1; i <= 5; i++) {
            String containerName = "container" + i;
            for (int j = 1; j <= 5; j++) {
                String kiePackageName = "rules.rule" + i + "_" + j;
                for (int k = 1; k <= 10; k++) {
                    DroolsRule droolsRule = new DroolsRule();
                    droolsRule.setRuleId((long) ((i - 1) * 50 + (j - 1) * 10 + k));
                    droolsRule.setContainerName(containerName);
                    droolsRule.setKieBaseName("kieBase" + i);
                    droolsRule.setKiePackageName(kiePackageName);
                    droolsRule.setRuleName("rule-" + i + "-" + j + "-" + k);
                    droolsRule.setIfCondition("$i: Integer(intValue >= " + (k - 1) + " && intValue <= " + (k + 1) + ")");
                    droolsRule.setThenCondition("System.out.println(drools.getRule().getPackageName() + \".\" + drools.getRule().getName() + \" Run the rule and passed parameter: \" + $i);");
                    droolsRuleService.addDroolsRule(droolsRule);
                }
            }
        }

        // Run the containers
        for (int i = 1; i <= 5; i++) {
            String containerName = "container" + i;
            String kieBaseName = "kieBase" + i;
            for (int param = 0; param < 12; param++) {
                droolsManager.fireRule(containerName, kieBaseName, param);
            }
        }
    }

    @Test
    public void testAddAndFireRules() throws InterruptedException {
        // Add 100 rules
        List<DroolsRule> rules = new ArrayList<>();
        for (long  i = 1; i <= 10; i++) {
            DroolsRule rule = new DroolsRule();
            rule.setRuleId(i);
            rule.setContainerName("container1");
            rule.setKieBaseName("kieBase01");
            rule.setKiePackageName("rules.rule" + i);
            rule.setRuleName("rule-" + i);
            rule.setIfCondition("$i: Integer(intValue >= " + (i * 3) + " && intValue <= " + (i * 5) + ")");
            rule.setThenCondition("System.out.println($i);");
            rules.add(rule);
            droolsManager2.addOrUpdateRule(rule);
        }

        // Fire rules concurrently
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 1; i <= 100; i++) {
            final int param = i * 4;
            executorService.execute(() -> {
                try {
                    droolsManager2.fireRule("container1", "kieBase01", param);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all rules to complete
        latch.await(60, TimeUnit.SECONDS);
        executorService.shutdown();
    }
}