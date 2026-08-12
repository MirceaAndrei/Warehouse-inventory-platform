package com.inventory.dashboard.service;

import com.inventory.dashboard.model.Item;
import com.inventory.dashboard.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryApiService {
    
    private final RestTemplate restTemplate;
    
    @Value("${inventory.api.url}")
    private String apiUrl;
    
    public List<Item> getAllItems() {
        try {
            System.out.println("DEBUG: Fetching items from: " + apiUrl + "/items");
            ResponseEntity<List<Item>> response = restTemplate.exchange(
                    apiUrl + "/items",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Item>>() {}
            );
            System.out.println("DEBUG: Got " + (response.getBody() != null ? response.getBody().size() : 0) + " items");
            return response.getBody();
        } catch (Exception e) {
            System.err.println("ERROR fetching items: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public List<Transaction> getAllTransactions() {
        try {
            System.out.println("DEBUG: Fetching transactions from: " + apiUrl + "/transactions");
            ResponseEntity<List<Transaction>> response = restTemplate.exchange(
                    apiUrl + "/transactions",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Transaction>>() {}
            );
            System.out.println("DEBUG: Got " + (response.getBody() != null ? response.getBody().size() : 0) + " transactions");
            return response.getBody();
        } catch (Exception e) {
            System.err.println("ERROR fetching transactions: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public Item createItem(String name, String barcode, Integer quantity, String location, String category) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("barcode", barcode);
        request.put("quantity", quantity);
        if (location != null && !location.isEmpty()) {
            request.put("location", location);
        }
        if (category != null && !category.isEmpty()) {
            request.put("category", category);
        }
        
        return restTemplate.postForObject(apiUrl + "/items", request, Item.class);
    }
    
    public Item updateItem(Long id, String name, String barcode, Integer quantity, String location, String category) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        request.put("barcode", barcode);
        request.put("quantity", quantity);
        if (location != null && !location.isEmpty()) {
            request.put("location", location);
        }
        if (category != null && !category.isEmpty()) {
            request.put("category", category);
        }
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request);
        ResponseEntity<Item> response = restTemplate.exchange(
                apiUrl + "/items/" + id,
                HttpMethod.PUT,
                entity,
                Item.class
        );
        return response.getBody();
    }
    
    public void deleteItem(Long id) {
        restTemplate.delete(apiUrl + "/items/" + id);
    }
    
    public void deleteTransaction(Long id) {
        restTemplate.delete(apiUrl + "/transactions/" + id);
    }
}
