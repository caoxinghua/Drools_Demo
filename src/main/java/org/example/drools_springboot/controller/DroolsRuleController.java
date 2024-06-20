package org.example.drools_springboot.controller;

import org.example.drools_springboot.DroolsManager;
import org.example.drools_springboot.entity.DroolsRule;
import org.example.drools_springboot.service.DroolsRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
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

    @GetMapping("fireRule")
    public String fireRule(@RequestParam String containerName, @RequestParam String kieBaseName, @RequestParam Integer param) {
        return droolsManager.fireRule(containerName, kieBaseName, param, null);
    }

    @PostMapping("fireRule")
    public String fireRuleWithJson(@RequestParam String containerName, @RequestParam String kieBaseName, @RequestBody Map<String, Object> payload) {
        return droolsManager.fireRule(containerName, kieBaseName, null, payload);
    }
}
