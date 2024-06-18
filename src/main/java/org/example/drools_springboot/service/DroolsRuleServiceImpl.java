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
        //1. DB is updated
        //2. not adding rule to container
        //3. create new container
        //4. add rule into Module
        //5. Add Module to container
        //6. run the rule
        //7. dispose()

        //Not this way
        droolsManager.addOrUpdateRule(droolsRule); //updating container

    }

    @Override
    public void updateDroolsRule(DroolsRule droolsRule) {
        droolsRule.validate();
        droolsRule.setUpdateTime(new Date());
        droolsRuleMap.put(droolsRule.getRuleId(), droolsRule);
        droolsManager.addOrUpdateRule(droolsRule);
    }
}
