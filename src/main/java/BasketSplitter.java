//package com.ocado.basket;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BasketSplitter {
    private Map<String, List<String>> deliveryConfig;

    public BasketSplitter(String absolutePathToConfigFile) {
        // Read the configuration file
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.deliveryConfig = objectMapper.readValue(new File(absolutePathToConfigFile), Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, List<String>> split(List<String> items) {
        // Check if the number of products in the basket does not exceed the limit
        if (items.size() > 100) {
            throw new IllegalArgumentException("The number of products in the basket exceeds the limit (100).");
        }

        Map<String, List<String>> deliveryGroups = new HashMap<>();
        Map<String, Integer> groupSizes = new HashMap<>(); // Map to store the sizes of delivery groups

        // For each product in the customer's basket
        for (String item : items) {
            // Check if the product is in the delivery configuration
            if (deliveryConfig.containsKey(item)) {
                List<String> deliveryOptions = deliveryConfig.get(item);
                String bestDeliveryOption = ""; // Best delivery option for the product
                int maxSize = -1; // Size of the largest delivery group

                // Choose the best delivery option for the product
                for (String deliveryOption : deliveryOptions) {
                    int currentSize = groupSizes.getOrDefault(deliveryOption, 0);
                    if (currentSize > maxSize) {
                        maxSize = currentSize;
                        bestDeliveryOption = deliveryOption;
                    }
                }

                // Add the product to the appropriate delivery group
                deliveryGroups.computeIfAbsent(bestDeliveryOption, k -> new ArrayList<>()).add(item);
                groupSizes.put(bestDeliveryOption, maxSize + 1); // Update the group size
            }
        }

        // Check if the number of delivery groups does not exceed the limit
        if (deliveryGroups.size() > 10) {
            throw new IllegalArgumentException("The number of different delivery methods exceeds the limit (10).");
        }

        // Sort the map by the number of products for each delivery method
        List<Map.Entry<String, List<String>>> sortedEntries = new ArrayList<>(deliveryGroups.entrySet());
        sortedEntries.sort((entry1, entry2) -> Integer.compare(entry2.getValue().size(), entry1.getValue().size()));

        // Display the sorted delivery groups
        System.out.println("\nProduct division into delivery groups (sorted):");
        for (Map.Entry<String, List<String>> entry : sortedEntries) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        // Return the sorted map of delivery groups
        Map<String, List<String>> sortedDeliveryGroups = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : sortedEntries) {
            sortedDeliveryGroups.put(entry.getKey(), entry.getValue());
        }
        return sortedDeliveryGroups;
    }
}