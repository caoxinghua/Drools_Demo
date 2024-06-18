package org.example.drools_springboot.controller;

import org.example.drools_springboot.DroolsManager2;
import org.example.drools_springboot.entity.DroolsRule;
import org.example.drools_springboot.service.DroolsRuleService2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/drools/rule2")
public class DroolsRuleController2 {

    @Autowired
    private DroolsRuleService2 droolsRuleService2;
    @Autowired
    private DroolsManager2 droolsManager2;

    @GetMapping("findAll")
    public List<DroolsRule> findAll() {
        return droolsRuleService2.findAll();
    }

    @PostMapping("add")
    public String addRule(@RequestBody DroolsRule droolsRule) {
        droolsRuleService2.addDroolsRule(droolsRule);
        return "Add rule Success";
    }

    @PostMapping("update")
    public String updateRule(@RequestBody DroolsRule droolsRule) {
        droolsRuleService2.updateDroolsRule(droolsRule);
        return "Update rule success";
    }

    @GetMapping("fireRule")
    public String fireRule(
            @RequestParam String containerName,
            @RequestParam String kieBaseName,
            @RequestParam Integer param
    ) {
        try {
            return droolsManager2.fireRule(containerName, kieBaseName, param);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fire rule", e);
        }
    }
}