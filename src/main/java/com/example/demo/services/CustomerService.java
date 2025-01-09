package com.example.demo.services;

import com.example.demo.models.Customer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Expression jsltExpression;

    @PostConstruct
    public void init() throws IOException {
        jsltExpression = Parser.compile(new File("src/main/resources/templates/transform-customer.jslt"));
    }

    public void getTransformedCustomers() {
        try {
            Customer[] customersArray = objectMapper.readValue(
                    new File("src/test/resources/input.json"), Customer[].class
            );
            List<Customer> customers = List.of(customersArray);

            JsonNode customersNode = objectMapper.valueToTree(customers);

            JsonNode transformedNode = jsltExpression.apply(customersNode);

            if (transformedNode.isArray()) {
                List<JsonNode> transformedCustomers = new ArrayList<>();
                transformedNode.elements().forEachRemaining(transformedCustomers::add);

                transformedCustomers = transformedCustomers.stream()
                        .filter(node -> node.get("isSubscribed") != null && node.get("isSubscribed").asBoolean())  // Only include subscribed customers
                        .collect(Collectors.toList());

                File outputFile = new File("src/main/resources/output.json");
                if (outputFile.exists()) {
                    objectMapper.writeValue(outputFile, "");
                }

                if (!transformedCustomers.isEmpty()) {
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, transformedCustomers);
                } else {
                    objectMapper.writeValue(outputFile, "No customers found");
                }
            } else {
                throw new IllegalStateException("Expected result to be array.");
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading input", e);
        }
    }


}
