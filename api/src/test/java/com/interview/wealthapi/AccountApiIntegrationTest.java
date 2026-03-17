package com.interview.wealthapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AccountApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldTransferFundsAndReturnStatement() throws Exception {
        String customerPayload = """
                {
                  "fullName": "Asha Verma",
                  "email": "asha.verma@example.com",
                  "riskProfile": "BALANCED"
                }
                """;

        MvcResult customerResult = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(customerPayload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode customerNode = objectMapper.readTree(customerResult.getResponse().getContentAsString());
        String customerId = customerNode.get("id").asText();

        String createAccountOne = """
                {
                  "customerId": "%s",
                  "currency": "USD",
                  "openingBalance": 1000.00
                }
                """.formatted(customerId);
        String createAccountTwo = """
                {
                  "customerId": "%s",
                  "currency": "USD",
                  "openingBalance": 300.00
                }
                """.formatted(customerId);

        MvcResult accountOneResult = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAccountOne))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult accountTwoResult = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createAccountTwo))
                .andExpect(status().isCreated())
                .andReturn();

        String sourceId = objectMapper.readTree(accountOneResult.getResponse().getContentAsString()).get("id").asText();
        String destinationId = objectMapper.readTree(accountTwoResult.getResponse().getContentAsString()).get("id").asText();

        String transferPayload = """
                {
                  "sourceAccountId": "%s",
                  "destinationAccountId": "%s",
                  "amount": 250.00,
                  "clientRequestId": "trf-2026-0001"
                }
                """.formatted(sourceId, destinationId);

        mockMvc.perform(post("/api/v1/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Transfer successful"));

        String from = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1).toString();
        String to = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toString();

        mockMvc.perform(get("/api/v1/accounts/{accountId}/statement", sourceId)
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].direction").value("DEBIT"));
    }
}
