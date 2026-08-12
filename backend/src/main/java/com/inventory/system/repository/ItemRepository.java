package com.inventory.system.repository;

import com.inventory.system.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    List<Item> findAllByOrderByIdDesc();
}
