package com.example.demo.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.schibsted.spt.data.jslt.Function;
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.mongodb.client.model.Filters.eq;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Expression jsltExpression;

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String dbName;

    @PostConstruct
    public void init() throws IOException {
        Collection<Function> functions = Collections.singleton(new LookUpFunction(mongoUri, dbName));
        jsltExpression = Parser.compile(new File("src/main/resources/templates/transform-customer.jslt"), functions);
    }

    public void getTransformedCustomers() {
        try {
            JsonNode customers = objectMapper.readTree(
                    new File("src/test/resources/input.json")
            );

            JsonNode transformedNode = jsltExpression.apply(customers);

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

    public static class LookUpFunction implements Function {

        private final MongoClient mongoClient;
        private final String dbName;

        public LookUpFunction(String connectionString, String dbName) {
            this.mongoClient = MongoClients.create(connectionString);
            this.dbName = dbName;
        }

        @Override
        public String getName() {
            return "Lookup";
        }

        @Override
        public int getMinArguments() {
            return 3;
        }

        @Override
        public int getMaxArguments() {
            return 3;
        }

        @Override
        public JsonNode call(JsonNode input, JsonNode[] params) {
            String collectionName = params[0].asText();
            JsonNode filterNode = params[1];
            String outputField = params[2].asText();

            String filterKey = filterNode.fieldNames().next();
            String filterValue = filterNode.get(filterKey).asText();

            MongoCollection<Document> collection = mongoClient.getDatabase(dbName).getCollection(collectionName);

            Document document = collection.find(eq(filterKey, filterValue)).first();

            if (document != null && document.containsKey(outputField)) {
                return new TextNode(document.getString(outputField));
            }

            return new TextNode("Hello");
        }
    }


}
