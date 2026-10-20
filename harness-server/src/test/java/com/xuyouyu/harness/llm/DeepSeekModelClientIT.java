package com.xuyouyu.harness.llm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

@SpringBootTest
public class DeepSeekModelClientIT {

    @Autowired
    private ModelClient modelClient;

    @Test
    public void testGenerate() {
        String response = modelClient.generate("你好");
        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isBlank());
    }
}
