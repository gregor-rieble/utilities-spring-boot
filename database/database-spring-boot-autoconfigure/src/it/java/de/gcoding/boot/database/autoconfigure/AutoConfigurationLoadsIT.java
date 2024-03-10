package de.gcoding.boot.database.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AutoConfigurationLoadsIT {
    @Autowired
    BeanFactory beanFactory;

    @Test
    void testConfigurationLoads() {
        assertThat(beanFactory.getBean(DatabaseAutoConfiguration.class)).isNotNull();
        assertThat(beanFactory.getBean(AuditorAware.class)).isNotNull();
        assertThat(beanFactory.getBean(DateTimeProvider.class)).isNotNull();
    }

    @SpringBootApplication
    static class TestContext {
    }
}
