package org.example.drools_springboot.service;

import org.example.drools_springboot.DroolsManager2;
import org.example.drools_springboot.entity.DroolsRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * drools rule service
 */
@Service
public class DroolsRuleServiceImpl2 implements DroolsRuleService2 {

    @Autowired
    private DroolsManager2 droolsManager2;

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

        droolsManager2.addOrUpdateRule(droolsRule);
    }

    @Override
    public void updateDroolsRule(DroolsRule droolsRule) {
        droolsRule.validate();
        droolsRule.setUpdateTime(new Date());
        droolsRuleMap.put(droolsRule.getRuleId(), droolsRule);

        droolsManager2.addOrUpdateRule(droolsRule);
    }
}