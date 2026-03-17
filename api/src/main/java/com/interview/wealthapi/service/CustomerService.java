package com.interview.wealthapi.service;

import com.interview.wealthapi.api.dto.CreateCustomerRequest;
import com.interview.wealthapi.domain.Customer;
import com.interview.wealthapi.repository.CustomerRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Customer createCustomer(CreateCustomerRequest request) {
        Customer customer = new Customer(
                UUID.randomUUID(),
                request.fullName(),
                request.email(),
                request.riskProfile()
        );
        return customerRepository.save(customer);
    }
}
