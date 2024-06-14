package org.example.drools_springboot.controller;

import org.example.drools_springboot.DroolsManager;
import org.example.drools_springboot.entity.DroolsRule;
import org.example.drools_springboot.service.DroolsRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/drools/rule")
public class DroolsRuleController {

    @Autowired
    private DroolsRuleService droolsRuleService;
    @Autowired
    private DroolsManager droolsManager;

    @GetMapping("findAll")
    public List<DroolsRule> findAll() {
        return droolsRuleService.findAll();
    }

    @PostMapping("add")
    public String addRule(@RequestBody DroolsRule droolsRule) {
        droolsRuleService.addDroolsRule(droolsRule);
        return "Add rule Success";
    }

    @PostMapping("update")
    public String updateRule(@RequestBody DroolsRule droolsRule) {
        droolsRuleService.updateDroolsRule(droolsRule);
        return "Update rule success";
    }

    @PostMapping("deleteRule")
    public String deleteRule(Long ruleId, String ruleName) {
        droolsRuleService.deleteDroolsRule(ruleId, ruleName);
        return "Delete rule success";
    }

    @GetMapping("fireRule")
    public String fireRule(String kieBaseName, Integer param) {
        return droolsManager.fireRule(kieBaseName, param);
    }
}
