package com.example.demo.services;

import com.example.demo.models.Customer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestCustomerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CustomerService customerService;

    @BeforeEach
    public void setUp() throws IOException{
        customerService = new CustomerService();
        customerService.init();
    }

    @Test
    public void testGetTransformedCustomersSuccess() throws IOException {
        Customer customer1 = new Customer(1, "Alice", "alice@example.com", 30, "active");
        Customer customer2 = new Customer(2, "Bob", "bob@example.com", 25, "inactive");
        List<Customer> customers = Arrays.asList(customer1, customer2);
        String inputJson = objectMapper.writeValueAsString(customers);
        Files.write(new File("src/test/resources/input.json").toPath(), inputJson.getBytes());

        customerService.getTransformedCustomers();

        File outputFile = new File("src/main/resources/output.json");
        assertTrue(outputFile.exists(), "Output file should exist");

        String outputJson = Files.readString(outputFile.toPath());
        List<JsonNode> transformedCustomers = Arrays.asList(objectMapper.readValue(outputJson, JsonNode[].class));

        assertEquals(1, transformedCustomers.size(), "Only one active customer should be there");
        JsonNode transformedCustomer = transformedCustomers.getFirst();
        assertEquals("Alice", transformedCustomer.get("name").asText(), "Name should match");
    }

    @Test
    public void testGetTransformedCustomersNoActiveCustomersFound() throws IOException {
        Customer customer1 = new Customer(1, "Alice", "alice@example.com", 30, "inactive");
        Customer customer2 = new Customer(2, "Bob", "bob@example.com", 25, "inactive");
        List<Customer> customers = Arrays.asList(customer1, customer2);
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
}
