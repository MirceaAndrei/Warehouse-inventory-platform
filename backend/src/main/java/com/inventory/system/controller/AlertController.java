package com.inventory.system.controller;

import com.inventory.system.domain.Item;
import com.inventory.system.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlertController {
    
    private final StockAlertService stockAlertService;
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<Item>> getLowStockItems() {
        return ResponseEntity.ok(stockAlertService.getLowStockItems());
    }
    
    @GetMapping("/critical-stock")
    public ResponseEntity<List<Item>> getCriticalStockItems() {
        return ResponseEntity.ok(stockAlertService.getCriticalStockItems());
    }
    
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getAlertSummary() {
        List<Item> lowStock = stockAlertService.getLowStockItems();
        List<Item> criticalStock = stockAlertService.getCriticalStockItems();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("lowStockCount", lowStock.size());
        summary.put("criticalStockCount", criticalStock.size());
        summary.put("lowStockItems", lowStock);
        summary.put("criticalStockItems", criticalStock);
        summary.put("totalAlerts", lowStock.size());
        
        return ResponseEntity.ok(summary);
    }
}
