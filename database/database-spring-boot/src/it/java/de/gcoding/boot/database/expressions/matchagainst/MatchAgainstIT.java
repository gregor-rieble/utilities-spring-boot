package de.gcoding.boot.database.expressions.matchagainst;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.internal.SessionFactoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static de.gcoding.boot.database.expressions.CustomExpressions.matchAgainst;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("it")
class MatchAgainstIT {
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private PersonEntityRepository personEntityRepository;

    @Test
    void testMatchAgainstFunctionContributorIsAutoRegistered() {
        final var sessionFactoryImpl = sessionFactory.unwrap(SessionFactoryImpl.class);
        final var serviceRegistry = sessionFactoryImpl.getServiceRegistry();
        final var functionContributors = serviceRegistry.requireService(ClassLoaderService.class)
            .loadJavaServices(FunctionContributor.class);

        assertThat(functionContributors).anyMatch(MatchAgainst.class::isInstance);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Peter", "pet%", "%ET%", "%tEr"})
    void testMatchAgainstFindsExpectedResults(String pattern) {
        givenThePersonsInDb("Peter", "Franz", "John", "Jonny", "Ronny");

        final Specification<PersonEntity> spec = (root, query, builder) ->
            builder.isTrue(matchAgainst(builder, pattern, root.get("name")));

        final var persons = personEntityRepository.findAll(spec);
        assertThat(persons)
            .hasSize(1)
            .first()
            .matches(person -> "Peter".equals(person.getName()));
    }

    @Test
    void testMatchAgainstWildcardCanBeEscaped() {
        givenThePersonsInDb("Peter", "Pe%");

        final var pattern = "Pe\\%";

        final Specification<PersonEntity> spec = (root, query, builder) ->
            builder.isTrue(matchAgainst(builder, pattern, root.get("name")));

        final var persons = personEntityRepository.findAll(spec);
        assertThat(persons)
            .hasSize(1)
            .first()
            .matches(person -> "Pe%".equals(person.getName()));
    }

    private void givenThePersonsInDb(String... names) {
        for (final var name : names) {
            final var person = new PersonEntity();
            person.setName(name);
            personEntityRepository.save(person);
        }
    }

    @SpringBootApplication
    @EntityScan(basePackageClasses = PersonEntity.class)
    @EnableJpaRepositories(basePackageClasses = PersonEntityRepository.class, considerNestedRepositories = true)
    public static class MatchAgainstITConfiguration {
    }

    @Entity
    public static class PersonEntity {
        @Id
        @Column(unique = true)
        private String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public interface PersonEntityRepository extends JpaRepository<PersonEntity, UUID>, JpaSpecificationExecutor<PersonEntity> {
    }
}
