package com.xuyouyu.harness.agent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("demo")
class DemoAgentPlannerTests {
    @Autowired
    private AgentRunner runner;

    @Test
    void executesRegisteredToolWithoutApiKey() {
        Assertions.assertEquals("统计完成，包含 ERROR 的行数：2",
                runner.run("INFO started\nERROR A\nERROR B"));
    }

    @Test
    void answersWithoutToolWhenNoErrorLineExists() {
        Assertions.assertEquals("演示模式回答：你好", runner.run("你好"));
    }
}
