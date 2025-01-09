package com.example.demo.services;

import com.example.demo.models.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class InputReader {

    public List<Customer> readCustomers(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        return List.of(mapper.readValue(new File(filePath), Customer[].class));
    }
}
