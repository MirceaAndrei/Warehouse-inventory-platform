package com.inventory.system.controller;

import com.inventory.system.domain.StockSettings;
import com.inventory.system.repository.StockSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockSettingsController {
    
    private final StockSettingsRepository stockSettingsRepository;
    
    @GetMapping
    public ResponseEntity<List<StockSettings>> getAllSettings() {
        return ResponseEntity.ok(stockSettingsRepository.findAll());
    }
    
    @GetMapping("/{key}")
    public ResponseEntity<StockSettings> getSettingByKey(@PathVariable String key) {
        return stockSettingsRepository.findBySettingKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSetting(@PathVariable String key, @RequestBody Map<String, Integer> request) {
        Integer value = request.get("value");
        if (value == null || value < 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid value"));
        }
        
        return stockSettingsRepository.findBySettingKey(key)
                .map(setting -> {
                    setting.setSettingValue(value);
                    stockSettingsRepository.save(setting);
                    return ResponseEntity.ok(setting);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockSettings> createSetting(@RequestBody StockSettings setting) {
        if (stockSettingsRepository.findBySettingKey(setting.getSettingKey()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(stockSettingsRepository.save(setting));
    }
}
