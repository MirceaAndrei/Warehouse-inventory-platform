package com.inventory.system.controller;

import com.inventory.system.domain.Item;
import com.inventory.system.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ItemController {
    
    private final InventoryService inventoryService;
    
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }
    
    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<Item> getItemByBarcode(@PathVariable String barcode) {
        try {
            return ResponseEntity.ok(inventoryService.getItemByBarcode(barcode));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> createItem(@RequestBody Item item) {
        try {
            return ResponseEntity.ok(inventoryService.createItem(item));
        } catch (RuntimeException e) {
            try {
                Item existing = inventoryService.getItemByBarcode(item.getBarcode());
                return ResponseEntity.status(409).body(java.util.Map.of(
                    "error", "Barcode already used",
                    "existingItem", existing
                ));
            } catch (RuntimeException ex) {
                return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
            }
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        try {
            System.out.println("📝 PUT /api/items/" + id + " - Received: " + item);
            return ResponseEntity.ok(inventoryService.updateItem(id, item));
        } catch (RuntimeException e) {
            System.err.println("❌ Error updating item " + id + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Object> deleteItem(@PathVariable Long id) {
        try {
            inventoryService.deleteItem(id);
            return ResponseEntity.noContent().build();
        } catch (com.inventory.system.exception.ItemDeleteException e) {
            System.err.println("[ERROR] Delete item blocked: " + e.getMessage());
            return ResponseEntity.status(409).body(java.util.Map.of(
                    "error", e.getMessage(),
                    "transactions", e.getTransactionCount()
            ));
        } catch (RuntimeException e) {
            System.err.println("[ERROR] Delete item failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/force")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> forceDeleteItem(@PathVariable Long id) {
        try {
            inventoryService.deleteItemWithTransactions(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.err.println("[ERROR] Force delete failed: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
