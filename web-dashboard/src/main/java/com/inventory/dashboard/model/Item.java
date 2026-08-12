package com.inventory.dashboard.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Item {
    private Long id;
    private String barcode;
    private String name;
    private String description;
    private Integer quantity;
    private Integer minQuantity;
    private String location;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
