package com.example.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TestCustomerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CustomerService customerService;


    @BeforeEach
    public void setUp(){
    }

    private JsonNode createCustomerJson(int id, String salutation, String name, String email, String datOfBirth,
                                        String schoolEnrollmentDate, String subscriptionStatus) {
        ObjectNode customerNode = objectMapper.createObjectNode();
        customerNode.put("id", id);
        customerNode.put("salutation", salutation);
        customerNode.put("name", name);
        customerNode.put("email", email);
        customerNode.put("dateOfBirth", datOfBirth);
        customerNode.put("schoolEnrollmentDate", schoolEnrollmentDate);
        customerNode.put("subscriptionStatus", subscriptionStatus);
        return customerNode;
    }

    @Test
    public void testGetTransformedCustomersSuccess() throws IOException {
        JsonNode customer1 = createCustomerJson(1, "Shri", "Alice", "alice@example.com",
                "28/09/2018", "12-09-2022", "active");
        JsonNode customer2 = createCustomerJson(2, "Smt", "Ema", "ema@example.com",
                "28/09/2019", "12-09-2023", "active");
        JsonNode customer3 = createCustomerJson(2, "Sh", "Bob", "bob@example.com",
                "28/09/2010", "12-09-2023", "inactive");

        List<JsonNode> customers = Arrays.asList(customer1, customer2, customer3);
        String inputJson = objectMapper.writeValueAsString(customers);
        Files.write(new File("src/test/resources/input.json").toPath(), inputJson.getBytes());

        customerService.getTransformedCustomers();

        File outputFile = new File("src/main/resources/output.json");
        assertTrue(outputFile.exists(), "Output file should exist");

        String outputJson = Files.readString(outputFile.toPath());
        List<JsonNode> transformedCustomers = Arrays.asList(objectMapper.readValue(outputJson, JsonNode[].class));

        assertEquals(2, transformedCustomers.size(), "Only two active customer should be there");
        JsonNode transformedCustomer = transformedCustomers.get(1);
        assertEquals("UKG", transformedCustomer.get("classStandard").asText(), "Class standard should match");
    }

    @Test
    public void testGetTransformedCustomersNoActiveCustomersFound() throws IOException {
        JsonNode customer1 = createCustomerJson(1, "Shri", "Alice", "alice@example.com",
                "28/09/2018", "12-09-2022", "inactive");
        JsonNode customer2 = createCustomerJson(2, "Smt", "Ema", "ema@example.com",
                "28/09/2019", "12-09-2023", "inactive");
        List<JsonNode> customers = Arrays.asList(customer1, customer2);
        String inputJson = objectMapper.writeValueAsString(customers);
        Files.write(new File("src/test/resources/input.json").toPath(), inputJson.getBytes());

        customerService.getTransformedCustomers();

        File outputFile = new File("src/main/resources/output.json");
        assertTrue(outputFile.exists(), "Output file should exist");
        assertEquals(Files.readString(outputFile.toPath()), "\"No customers found\"", "Output should contain 'No customers found'");
    }

    @Test
    public void testGetTransformedCustomers_InvalidInput() throws IOException {
        Files.write(new File("src/test/resources/input.json").toPath(), "invalid json".getBytes());

        RuntimeException exception = null;
        try {
            customerService.getTransformedCustomers();
        } catch (RuntimeException e) {
            exception = e;
        }

        assertNotNull(exception, "RuntimeException should be thrown for invalid input JSON");
        assertTrue(exception.getMessage().contains("Error reading input"), "Error message should display input reading issue");
    }

    @Test
    public void testGetTransformedCustomersInvalidDateFormat() throws IOException {
        // Create a customer with an invalid date format for the "dateOfBirth" field
        JsonNode customer1 = createCustomerJson(1, "Shri", "Alice", "alice@example.com",
                "invalid date", "12-09-2022", "active");
        JsonNode customer2 = createCustomerJson(2, "Smt", "Ema", "ema@example.com",
                "28/09/2019", "12-09-2023", "active");

        List<JsonNode> customers = Arrays.asList(customer1, customer2);
        String inputJson = objectMapper.writeValueAsString(customers);
        Files.write(new File("src/test/resources/input.json").toPath(), inputJson.getBytes());

        RuntimeException exception = null;
        try {
            customerService.getTransformedCustomers();
        } catch (RuntimeException e) {
            exception = e;
        }

        assertNotNull(exception, "Exception should be thrown for invalid date format");
        assertTrue(exception.getMessage().contains("Invalid date or date format"),
                "Error message should display invalid date or format");
    }

}
