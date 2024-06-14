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
