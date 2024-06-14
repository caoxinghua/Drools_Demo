package org.example.drools_springboot.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
public class DroolsRule {

    /**
     * rule id
     */
    private Long ruleId;
    /**
     * kContainer Name
     */
    private String containerName;
    /**
     * kBase Name
     */
    private String kieBaseName;
    /**
     * if kiePackageName=rules/rule01 then the path is： kieFileSystem.write("src/main/resources/rules/rule01/1.drl")
     */
    private String kiePackageName;
    /**
     * rule content
     */
    private String ruleContent;
    /**
     * create time
     */
    private Date createdTime;
    /**
     * update time
     */
    private Date updateTime;

    public void validate() {
        if (this.ruleId == null || isBlank(kieBaseName) || isBlank(kiePackageName) || isBlank(ruleContent)) {
            throw new RuntimeException("Validation failed");
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.isEmpty();
    }
}
