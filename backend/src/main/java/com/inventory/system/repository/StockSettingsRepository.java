package com.inventory.system.repository;

import com.inventory.system.domain.StockSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockSettingsRepository extends JpaRepository<StockSettings, Long> {
    Optional<StockSettings> findBySettingKey(String settingKey);
}
