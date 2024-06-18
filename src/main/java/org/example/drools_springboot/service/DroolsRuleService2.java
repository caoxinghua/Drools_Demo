package org.example.drools_springboot.service;

import org.example.drools_springboot.entity.DroolsRule;

import java.util.List;

/**
 * drools rule service
 */
public interface DroolsRuleService2 {

    List<DroolsRule> findAll();

    void addDroolsRule(DroolsRule droolsRule);

    void updateDroolsRule(DroolsRule droolsRule);
}
