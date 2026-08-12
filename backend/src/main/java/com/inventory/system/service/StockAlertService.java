package com.inventory.system.service;

import com.inventory.system.domain.Item;
import com.inventory.system.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockAlertService {
    
    private final ItemRepository itemRepository;
    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int CRITICAL_STOCK_THRESHOLD = 2;
    
    

    @Scheduled(fixedRate = 1800000) 
    public void checkLowStock() {
        List<Item> lowStockItems = itemRepository.findAll().stream()
                .filter(item -> item.getQuantity() <= LOW_STOCK_THRESHOLD)
                .collect(Collectors.toList());
        
        if (!lowStockItems.isEmpty()) {
            log.warn("[WARNING] ALERTA: {} produse cu stoc scazut!", lowStockItems.size());
            
            for (Item item : lowStockItems) {
                if (item.getQuantity() <= CRITICAL_STOCK_THRESHOLD) {
                    log.error("🔴 CRITIC: {} (barcode: {}) - doar {} bucăți rămase!", 
                            item.getName(), item.getBarcode(), item.getQuantity());
                } else {
                    log.warn("🟡 Stoc scăzut: {} (barcode: {}) - {} bucăți rămase", 
                            item.getName(), item.getBarcode(), item.getQuantity());
                }
            }
        } else {
            log.info("[OK] Toate produsele au stoc adecvat");
        }
    }
    
    

    public List<Item> getLowStockItems() {
        return itemRepository.findAll().stream()
                .filter(item -> item.getQuantity() <= LOW_STOCK_THRESHOLD)
                .collect(Collectors.toList());
    }
    
    

    public List<Item> getCriticalStockItems() {
        return itemRepository.findAll().stream()
                .filter(item -> item.getQuantity() <= CRITICAL_STOCK_THRESHOLD)
                .collect(Collectors.toList());
    }
    
    

    public boolean isLowStock(Item item) {
        return item.getQuantity() <= LOW_STOCK_THRESHOLD;
    }
    
    

    public boolean isCriticalStock(Item item) {
        return item.getQuantity() <= CRITICAL_STOCK_THRESHOLD;
    }
}
