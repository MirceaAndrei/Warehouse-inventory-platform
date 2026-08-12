package com.inventory.system.repository;

import com.inventory.system.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByItemIdOrderByTimestampDesc(Long itemId);
    
    List<Transaction> findByItemId(Long itemId);
    
    void deleteByItemId(Long itemId);
    
    List<Transaction> findAllByOrderByTimestampDesc();
}
