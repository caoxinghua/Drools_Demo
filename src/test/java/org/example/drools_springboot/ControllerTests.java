package org.example.drools_springboot;

import org.example.drools_springboot.entity.DroolsRule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testAddRule() {
        // Create a sample DroolsRule object
        DroolsRule droolsRule = new DroolsRule();
        droolsRule.setRuleId(1L);
        droolsRule.setKieBaseName("kieBase01");
        droolsRule.setKiePackageName("rules.rule01");
        droolsRule.setRuleContent("package rules.rule01 \n" +
                " global java.lang.StringBuilder resultInfo \n" +
                " rule \"rule-01\" when $i: Integer() then" +
                " resultInfo.append(drools.getRule().getPackageName()).append(\".\").append(drools.getRule().getName()).append(\"Run the rule and passed paramter: \").append($i); end");

        // Set the request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the request entity with the DroolsRule object and headers
        HttpEntity<DroolsRule> requestEntity = new HttpEntity<>(droolsRule, headers);

        // Send the POST request to the /drools/rule/add endpoint
        ResponseEntity<String> response = restTemplate.postForEntity("/drools/rule/add", requestEntity, String.class);

        // Assert the response status code
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Assert the response body
        assertEquals("Add rule Success", response.getBody());
    }
}