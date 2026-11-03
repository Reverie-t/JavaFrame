package org.example.javaframe;

import org.example.javaframe.auth.DemoUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JavaFrameApplicationTests {

    @Autowired
    private DemoUser demoUser;

    @Test
    void contextLoads() {
    }

    @Test
    void demoUserAcceptsOnlyTheConfiguredCredentials() {
        assertThat(demoUser.matches("admin", "admin123")).isTrue();
        assertThat(demoUser.matches("admin", "wrong")).isFalse();
        assertThat(demoUser.matches("nobody", "admin123")).isFalse();
    }
}
