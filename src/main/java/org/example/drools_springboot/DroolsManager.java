package org.example.drools_springboot;

import org.example.drools_springboot.entity.DroolsRule;
import lombok.extern.slf4j.Slf4j;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;


@Component
@Slf4j
public class DroolsManager {

    // Single Service
    private final KieServices kieServices = KieServices.get();
    // Init File System and Module config for Service
    private final KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
    private final KieModuleModel kieModuleModel = kieServices.newKieModuleModel();

    // Single Container
    private KieContainer kieContainer;

    /**
     * If KBase exists
     */
    public boolean existsKieBase(String kieBaseName) {
        if (null == kieContainer) {
            return false;
        }
        Collection<String> kieBaseNames = kieContainer.getKieBaseNames();
        if (kieBaseNames.contains(kieBaseName)) {
            return true;
        }
        log.info("Missing KieBase:{}", kieBaseName);
        return false;
    }

    public void deleteDroolsRule(DroolsRule droolsRule, String ruleName) {
        String kieBaseName = droolsRule.getKieBaseName();
        String packageName = droolsRule.getKiePackageName();
        if (existsKieBase(kieBaseName)) {
            KieBase kieBase = kieContainer.getKieBase(kieBaseName);
            kieBase.removeRule(packageName, ruleName);
            String file = "src/main/resources/" + droolsRule.getKiePackageName() + "/" + droolsRule.getRuleId() + ".drl";
            kieFileSystem.delete(file);
            buildKieContainer();
            log.info("delete kieBase: [{}] package: [{}] inside rule file: [{}]", kieBaseName, packageName, ruleName);
        }
    }

    /**
     * Add or update rule file
     */
    public void addOrUpdateRule(DroolsRule droolsRule) {
        // get kBase name
        String kieBaseName = droolsRule.getKieBaseName();
        // check kBase exist
        boolean existsKieBase = existsKieBase(kieBaseName);
        // create kmodule.xml inside kBase tag
        KieBaseModel kieBaseModel = null;
        if (!existsKieBase) {
            // create a not default Kie Module
            kieBaseModel = kieModuleModel.newKieBaseModel(kieBaseName);
            kieBaseModel.setDefault(false);

            // Add rule package path and kieSession
            kieBaseModel.addPackage(droolsRule.getKiePackageName());
            kieBaseModel.newKieSessionModel(kieBaseName + "-session")
                    .setDefault(false);
        } else {
            // Get exist kBase Object
            kieBaseModel = kieModuleModel.getKieBaseModels().get(kieBaseName);
            // Get packages
            List<String> packages = kieBaseModel.getPackages();
            String packageName = droolsRule.getKiePackageName();

            if (!packages.contains(packageName)) {
                kieBaseModel.addPackage(droolsRule.getKiePackageName());
                log.info("kieBase: {} add a new rule package:{}", kieBaseName, droolsRule.getKiePackageName());
            }
        }

        /**
         * a logical path within the KieFileSystem, not a physical file path on the local file system
         * **/
        String file = "src/main/resources/" + droolsRule.getKiePackageName() + "/" + droolsRule.getRuleId() + ".drl";
        log.info("Load rules: {}", file);
        kieFileSystem.write(file, droolsRule.getRuleContent());
        /**
         * it writes the rule content to a virtual file system maintained by the `KieFileSystem`, not to the actual file system on disk.
         * The KieFileSystem acts as an in-memory representation of the rule files and resources.
         * Can modify rules dynamically at runtime without the need to restart the application
         * It's an abstraction layer provided by Drools to manage rule files in memory
         * **/

        String kmoduleXml = kieModuleModel.toXML();
        log.info("Load kmodule.xml:[\n{}]", kmoduleXml);
        kieFileSystem.writeKModuleXML(kmoduleXml);

        buildKieContainer();
    }

    /**
     * Create KieContainer
     */
    private void buildKieContainer() {
        /**
         * The KieBuilder uses the KieFileSystem to build the KieModule, which contains the compiled rules and other resources.
         * **/
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        // Create KieModule inside all KieBase
        kieBuilder.buildAll();
        Results results = kieBuilder.getResults();
        // Error Msg
        List<Message> messages = results.getMessages(Message.Level.ERROR);
        if (null != messages && !messages.isEmpty()) {
            for (Message message : messages) {
                log.error(message.getText());
            }
            throw new RuntimeException("Loading rules failed");
        }
        // Get KieContainer
        if (null == kieContainer) {
            kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        } else {
            // Dynamic Update Container
            ((KieContainerImpl) kieContainer).updateToKieModule((InternalKieModule) kieBuilder.getKieModule());
        }
    }

    /**
     * Fire Rule
     */
    public String fireRule(String kieBaseName, Integer param) {
        // kieSession
        KieSession kieSession = kieContainer.newKieSession(kieBaseName + "-session");
        StringBuilder resultInfo = new StringBuilder();
        kieSession.setGlobal("resultInfo", resultInfo);
        kieSession.insert(param);
        kieSession.fireAllRules();
        kieSession.dispose();
        return resultInfo.toString();
    }
}
