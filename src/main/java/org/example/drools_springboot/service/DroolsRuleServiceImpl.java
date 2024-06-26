package org.example.drools_springboot.service;

import org.example.drools_springboot.DroolsManager;
import org.example.drools_springboot.entity.DroolsRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * drools rule service
 */
@Service
public class DroolsRuleServiceImpl implements DroolsRuleService {

    @Autowired
    private DroolsManager droolsManager;

    /**
     * Memory Storage
     */
    private Map<Long, DroolsRule> droolsRuleMap = new HashMap<>(16);

    @Override
    public List<DroolsRule> findAll() {
        return new ArrayList<>(droolsRuleMap.values());
    }

    @Override
    public void addDroolsRule(DroolsRule droolsRule) {
        droolsRule.validate();
        droolsRule.setCreatedTime(new Date());
        droolsRuleMap.put(droolsRule.getRuleId(), droolsRule);
        if(null == droolsRule.getRuleContent()) {
            // Construct the ruleContent string
            String ruleContent = "package " + droolsRule.getKiePackageName() + "\n" +
                    "rule \"" + droolsRule.getRuleName() + "\"\n" +
                    "when\n" +
                    "    " + droolsRule.getIfCondition() + "\n" +
                    "then\n" +
                    "    " + droolsRule.getThenCondition() + "\n" +
                    "end";

            droolsRule.setRuleContent(ruleContent);
        }

        droolsManager.addOrUpdateRule(droolsRule);
    }

    @Override
    public void updateDroolsRule(DroolsRule droolsRule) {
        droolsRule.validate();
        droolsRule.setUpdateTime(new Date());
        droolsRuleMap.put(droolsRule.getRuleId(), droolsRule);

        // Construct the ruleContent string
        String ruleContent = "package " + droolsRule.getKiePackageName() + "\n" +
                "rule \"" + droolsRule.getRuleName() + "\"\n" +
                "when\n" +
                "    " + droolsRule.getIfCondition() + "\n" +
                "then\n" +
                "    " + droolsRule.getThenCondition() + "\n" +
                "end";

        droolsRule.setRuleContent(ruleContent);
        droolsManager.addOrUpdateRule(droolsRule);
    }
}
