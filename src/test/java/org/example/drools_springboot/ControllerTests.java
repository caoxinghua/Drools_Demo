package org.example.drools_springboot;

import org.example.drools_springboot.entity.DroolsRule;
import org.example.drools_springboot.service.DroolsRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ControllerTests {

    @Autowired
    private DroolsRuleService droolsRuleService;

    @Autowired
    private DroolsManager droolsManager;

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
                    droolsRule.setRuleContent("package " + kiePackageName + "\n" +
                            "rule \"rule-" + i + "-" + j + "-" + k + "\"\n" +
                            "when\n" +
                            "    $i: Integer(intValue >= " + (k - 1) + " && intValue <= " + (k + 1) + ")\n" +
                            "then\n" +
                            "    System.out.println(drools.getRule().getPackageName() + \".\" + drools.getRule().getName() + \" Run the rule and passed parameter: \" + $i);\n" +
                            "end");
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
}