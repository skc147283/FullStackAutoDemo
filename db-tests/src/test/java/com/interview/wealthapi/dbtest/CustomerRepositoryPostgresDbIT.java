package com.interview.wealthapi.dbtest;

import com.interview.wealthapi.domain.Customer;
import com.interview.wealthapi.domain.RiskProfile;
import com.interview.wealthapi.repository.CustomerRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class CustomerRepositoryPostgresDbIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("wealthdb")
            .withUsername("wealth_user")
            .withPassword("wealth_pwd");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldPersistAndReadCustomerUsingPostgres() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "Postgres Test User", "postgres.user@example.com", RiskProfile.BALANCED);

        customerRepository.save(customer);

        Optional<Customer> reloaded = customerRepository.findById(customerId);
        assertTrue(reloaded.isPresent());
        assertEquals("postgres.user@example.com", reloaded.get().getEmail());
        assertEquals(RiskProfile.BALANCED, reloaded.get().getRiskProfile());
    }
}
