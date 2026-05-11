package com.zhm.springboot.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
        // 验证 Spring 上下文加载成功
    }
}