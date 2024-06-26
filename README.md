

[Drools Docs](https://docs.drools.org/8.44.0.Final/drools-docs/drools/KIE/index.html "Drools Docs")

### Introduction

1. KieServices:

    - KieServices is the main entry point for accessing the Drools engine.
    - It provides methods to create and manage various Drools components, such as KieFileSystem, KieModuleModel, KieBuilder, and KieContainer.

2. KieFileSystem:

    - KieFileSystem is a virtual file system used to store and manage the rule files and other resources.
    - It allows you to add, update, or delete rule files programmatically.

3. KieModuleModel:

    - KieModuleModel represents the configuration of a KIE module, which is a container for one or more KIE bases and their associated KIE sessions.
    - It allows you to define the structure and properties of the KIE module, such as the KIE bases, KIE sessions, and their configurations.

4. KieContainer:

    - KieContainer is a runtime container that manages the deployment of KIE modules.
    - It provides access to the KIE bases and KIE sessions defined in the KIE module.

5. KieBase:

    - KieBase represents a knowledge base, which is a repository of rules and other knowledge definitions.

    - It contains one or more KIE packages and provides methods to manage and execute the rules.

    - It can load multiple packages.

      ```xml
      <kbase name="kbase-01" packages="com.example.package1, com.example.package2">
          <ksession name="ksession-1" type="stateful" default="true"/>
      </kbase>
      ```

6. KiePackage:

    - KiePackage represents a package of rules and other knowledge definitions within a KieBase.
    - It groups related rules and resources as a logical grouping mechanism for organizing rules and resources.
    - We can organize rules into different KIE packages based on their functionality or domain.
    - Each KIE package can have its own set of drl rule files and other resources.
    - `KiePackages` can import other `KiePackages`, enabling reuse of assets across different packages and projects.


### Version 8 Upgrade

There are several significant differences between Drools 7.x and Drools 8 versions. Here are some of the major changes:

1. Java Version Support:

   Drools 7.x supports Java 8 and Java 11. Drools 8 requires Java 11 or later versions.

2. Module System:

   Drools 8 introduced a new module system based on the Java Platform Module System (JPMS) introduced in Java 9. This means that Drools 8 is modularized and provides better encapsulation and dependency management.

3. Rule Engine Improvements:

   Drools 8 includes improvements to the rule engine, such as better performance, reduced memory footprint, and enhanced rule evaluation strategies.

4. API Changes:

   Drools 8 made some changes to the API to align with the module system and to provide a more fluent and intuitive user experience. Some packages and classes have been relocated or renamed to fit better with the modularized structure.

5. Business Central and KIE Server:

   Drools 8 introduces a new version of Business Central, the web-based authoring and management tool for Drools. It also includes enhancements to the KIE Server, which is the runtime engine for executing rules and processes.


6. Integration with Other Technologies:

   Drools 8 has improved integration with other technologies and frameworks, such as Spring Boot and Quarkus. It provides better support for cloud-native deployments and containerization.