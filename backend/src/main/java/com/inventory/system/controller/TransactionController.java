package com.inventory.system.controller;

import com.inventory.system.domain.Transaction;
import com.inventory.system.service.InventoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {
    
    private final InventoryService inventoryService;
    
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(inventoryService.getAllTransactions());
    }
    
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<Transaction>> getTransactionsByItemId(@PathVariable Long itemId) {
        return ResponseEntity.ok(inventoryService.getTransactionsByItemId(itemId));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        try {
            inventoryService.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/scan")
    public ResponseEntity<Transaction> processScan(@RequestBody ScanRequest request) {
        try {
            System.out.println("[SCAN] RECEIVED: " + request.getBarcode() + " | Type: " + request.getType() + " | Qty: " + request.getQuantity());
            
            Transaction transaction = inventoryService.processTransaction(
                    request.getBarcode(),
                    request.getName(),
                    request.getType(),
                    request.getQuantity(),
                    request.getNotes(),
                    request.getDeviceId(),
                    request.getLocation(),
                    request.getCategory()
            );
            System.out.println("[SUCCESS] Transaction saved with ID " + transaction.getId());
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            System.err.println("[ERROR] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    
    @PostMapping
    public ResponseEntity<Transaction> processTransaction(@RequestBody java.util.Map<String, Object> request) {
        try {
            String barcode = (String) request.get("barcode");
            String name = (String) request.getOrDefault("name", null);
            String typeStr = (String) request.get("type");
            Integer quantity = (Integer) request.get("quantity");
            String notes = (String) request.getOrDefault("notes", null);
            String deviceId = (String) request.getOrDefault("deviceId", null);
            String location = (String) request.getOrDefault("location", null);
            String category = (String) request.getOrDefault("category", null);
            
            Transaction.TransactionType type = Transaction.TransactionType.valueOf(typeStr);
            
            Transaction transaction = inventoryService.processTransaction(
                    barcode, name, type, quantity, notes, deviceId, location, category
            );
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/sync")
    public ResponseEntity<List<Transaction>> syncTransactions(@RequestBody List<Transaction> offlineTransactions) {
        try {
            List<Transaction> synced = inventoryService.syncTransactions(offlineTransactions);
            return ResponseEntity.ok(synced);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Data
    public static class ScanRequest {
        private String barcode;
        private String name; 
        private Transaction.TransactionType type;
        private Integer quantity;
        private String notes;
        private String deviceId;
        private String location;
        private String category;
    }
}
