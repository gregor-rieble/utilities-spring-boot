package de.gcoding.boot.businessevents.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AutoConfigurationLoadsIT {
    @Autowired
    BeanFactory beanFactory;

    @Test
    void testConfigurationLoads() {
        assertThat(beanFactory.getBean(BusinessEventsAutoConfiguration.class)).isNotNull();
    }

    @SpringBootApplication
    static class TestContext {
    }
}
