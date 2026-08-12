package com.inventory.system.service;

import com.inventory.system.domain.Item;
import com.inventory.system.domain.Transaction;
import com.inventory.system.repository.ItemRepository;
import com.inventory.system.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final ItemRepository itemRepository;
    private final TransactionRepository transactionRepository;
    
    
    public List<Item> getAllItems() {
        return itemRepository.findAllByOrderByIdDesc();
    }
    
    public Item getItemByBarcode(String barcode) {
        return itemRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Item not found with barcode: " + barcode));
    }
    
    public Item createItem(Item item) {
        if (itemRepository.existsByBarcode(item.getBarcode())) {
            throw new RuntimeException("Item with barcode " + item.getBarcode() + " already exists");
        }
        Item saved = itemRepository.save(item);
        
        if (saved.getQuantity() != null && saved.getQuantity() > 0) {
            Transaction tx = new Transaction();
            tx.setItem(saved);
            tx.setType(Transaction.TransactionType.IN);
            tx.setQuantity(saved.getQuantity());
            tx.setNotes("Initial stock — added via Web UI");
            tx.setDeviceId("WEB-UI");
            tx.setSynced(true);
            transactionRepository.save(tx);
        }
        return saved;
    }
    
    public Item updateItem(Long id, Item itemDetails) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));

        Integer oldQuantity = item.getQuantity();

        
        if (itemDetails.getName() != null)        item.setName(itemDetails.getName());
        if (itemDetails.getBarcode() != null)     item.setBarcode(itemDetails.getBarcode());
        if (itemDetails.getQuantity() != null)    item.setQuantity(itemDetails.getQuantity());
        if (itemDetails.getDescription() != null) item.setDescription(itemDetails.getDescription());
        if (itemDetails.getLocation() != null)    item.setLocation(itemDetails.getLocation());
        if (itemDetails.getCategory() != null)    item.setCategory(itemDetails.getCategory());
        if (itemDetails.getMinQuantity() != null) item.setMinQuantity(itemDetails.getMinQuantity());

        System.out.println("📝 Updating item ID " + id + ": " + item.getName() + " | Qty: " + item.getQuantity());
        Item saved = itemRepository.save(item);

        
        if (itemDetails.getQuantity() != null && !itemDetails.getQuantity().equals(oldQuantity)) {
            Transaction tx = new Transaction();
            tx.setItem(saved);
            tx.setType(Transaction.TransactionType.ADJUST);
            tx.setQuantity(saved.getQuantity());
            tx.setNotes("Stock adjusted via Web UI (was " + oldQuantity + ")");
            tx.setDeviceId("WEB-UI");
            tx.setSynced(true);
            transactionRepository.save(tx);
        }
        return saved;
    }
    
    

    public void updateItemMetadata(String barcode, String location, String category) {
        Item item = itemRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Item not found with barcode: " + barcode));
        
        if (location != null && !location.isEmpty()) {
            item.setLocation(location);
            System.out.println("📍 Updated location for " + item.getName() + ": " + location);
        }
        
        if (category != null && !category.isEmpty()) {
            item.setCategory(category);
            System.out.println("🏷️ Updated category for " + item.getName() + ": " + category);
        }
        
        itemRepository.save(item);
    }
    
    public void deleteItem(Long id) {
        System.out.println("🗑️ Attempting to delete item ID: " + id);
        
        List<Transaction> transactions = transactionRepository.findByItemId(id);
        System.out.println("[INFO] Found " + transactions.size() + " transactions for item ID " + id);
        if (!transactions.isEmpty()) {
            String errorMsg = "Cannot delete item with existing transactions. Delete " + transactions.size() + " transactions first!";
            System.err.println("[ERROR] " + errorMsg);
            throw new com.inventory.system.exception.ItemDeleteException(errorMsg, transactions.size());
        }
        itemRepository.deleteById(id);
        System.out.println("[SUCCESS] Item ID " + id + " deleted successfully");
    }
    
    @Transactional
    public void deleteItemWithTransactions(Long id) {
        
        transactionRepository.deleteByItemId(id);
        
        itemRepository.deleteById(id);
    }
    
    
    @Transactional
    public Transaction processTransaction(String barcode, String name, Transaction.TransactionType type, 
                                         Integer quantity, String notes, String deviceId,
                                         String location, String category) {
        
        Item item;
        try {
            item = getItemByBarcode(barcode);
            
            
            boolean updated = false;
            if (location != null && !location.isEmpty()) {
                item.setLocation(location);
                updated = true;
            }
            if (category != null && !category.isEmpty()) {
                item.setCategory(category);
                updated = true;
            }
            if (updated) {
                System.out.println("📝 Updated metadata for: " + item.getName());
            }
        } catch (RuntimeException e) {
            
            item = new Item();
            item.setBarcode(barcode);
            item.setName(name != null && !name.isEmpty() ? name : "Scanned Item - " + barcode);
            item.setQuantity(0);
            item.setLocation(location != null && !location.isEmpty() ? location : null);
            item.setCategory(category != null && !category.isEmpty() ? category : "Uncategorized");
            item.setMinQuantity(5);
            item = itemRepository.save(item);
            System.out.println("✨ Created new item: " + item.getName() + " (" + barcode + ")");
        }
        
        
        switch (type) {
            case IN:
                item.setQuantity(item.getQuantity() + quantity);
                break;
            case OUT:
                if (item.getQuantity() < quantity) {
                    throw new RuntimeException("Insufficient quantity. Available: " + item.getQuantity());
                }
                item.setQuantity(item.getQuantity() - quantity);
                break;
            case ADJUST:
                item.setQuantity(quantity); 
                break;
        }
        
        itemRepository.save(item);
        
        
        Transaction transaction = new Transaction();
        transaction.setItem(item);
        transaction.setType(type);
        transaction.setQuantity(quantity);
        transaction.setNotes(notes);
        transaction.setDeviceId(deviceId);
        transaction.setSynced(true);
        
        return transactionRepository.save(transaction);
    }
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByTimestampDesc();
    }
    
    public List<Transaction> getTransactionsByItemId(Long itemId) {
        return transactionRepository.findByItemIdOrderByTimestampDesc(itemId);
    }
    
    @Transactional
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
    
    
    @Transactional
    public List<Transaction> syncTransactions(List<Transaction> offlineTransactions) {
        return offlineTransactions.stream()
                .map(t -> processTransaction(
                        t.getItem().getBarcode(),
                        t.getItem().getName(), 
                        t.getType(),
                        t.getQuantity(),
                        t.getNotes(),
                        t.getDeviceId(),
                        t.getItem().getLocation(), 
                        t.getItem().getCategory()  
                ))
                .toList();
    }
}
