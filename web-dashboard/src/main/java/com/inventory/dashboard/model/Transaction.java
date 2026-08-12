package com.inventory.dashboard.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Transaction {
    private Long id;
    private Item item;
    private String type;
    private Integer quantity;
    private String notes;
    private LocalDateTime timestamp;
    private String deviceId;
    private Boolean synced;
    private LocalDateTime createdAt;
}
