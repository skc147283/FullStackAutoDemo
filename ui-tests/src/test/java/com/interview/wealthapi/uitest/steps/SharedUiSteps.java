package com.interview.wealthapi.uitest.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base class for shared UI test step definitions.
 * Contains common utilities and helpers used across multiple step definition classes.
 * 
 * This eliminates code duplication across SmokeAndSanitySteps, CriticalWealthFlowSteps,
 * and CriticalBusinessUiNgIT.
 */
public class SharedUiSteps {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extracts a field value from a JSON response string.
     * 
     * @param json The JSON response as a string
     * @param fieldName The field name to extract
     * @return The field value as a string
     * @throws IllegalStateException if field not found or parsing fails
     */
    protected String extractField(String json, String fieldName) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode valueNode = node.get(fieldName);
            if (valueNode == null || valueNode.isNull()) {
                throw new IllegalStateException(
                        "Field '" + fieldName + "' not found in response: " + json);
            }
            return valueNode.asText();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Cannot parse field '" + fieldName + "' from: " + json, ex);
        }
    }

    /**
     * Checks if a field exists in a JSON response.
     * 
     * @param json The JSON response as a string
     * @param fieldName The field name to check
     * @return true if field exists and is not null, false otherwise
     */
    protected boolean fieldExists(String json, String fieldName) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode valueNode = node.get(fieldName);
            return valueNode != null && !valueNode.isNull();
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Parses a JSON string to a JsonNode for more complex extraction logic.
     * 
     * @param json The JSON response as a string
     * @return The parsed JsonNode
     * @throws IllegalStateException if parsing fails
     */
    protected JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot parse JSON: " + json, ex);
        }
    }
}
